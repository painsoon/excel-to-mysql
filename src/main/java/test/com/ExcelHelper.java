package test.com;

import java.io.File;
import java.io.FileInputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelHelper {
	DBManager dbManage = DBManager.getInstance();
	@SuppressWarnings("resource")
	public void importData(File file) throws Exception {
		if (!file.exists() || !file.isFile()) {
			throw new Exception("文件不对");
		}
		String suffix = file.getName().split("\\.")[1];
		Workbook wb = null;
		if ("xls".equals(suffix)) {
			FileInputStream fis = new FileInputStream(file); // 文件流对象
			wb = new HSSFWorkbook(fis);
		} else if ("xlsx".equals(suffix)) {
			wb = new XSSFWorkbook(file);
		} else {
			throw new Exception("文件不对");
		}

		Iterator<Sheet> sheets = wb.iterator();

		while (sheets.hasNext()) {
			Sheet sheet = sheets.next();
			String tableName = sheet.getSheetName();
			if(tableName.contains("说明")) {
				continue;
			}
			FieldBean fBean = getPrimaryField(sheet);
			if (fBean == null) {
				throw new Exception("主键为空");
			}
			
			List<FieldBean> listf = getField(sheet);
			dbManage.createTable(tableName, fBean, listf);
			dbManage.insertdate(tableName, getSheetData(sheet, listf));
		}

	}

	public List<List<String>> getSheetData(Sheet sheet, List<FieldBean> listf) {
		List<List<String>> list = new ArrayList<List<String>>();
		int rows = sheet.getLastRowNum();
		List<String> listdata = null;
		//第5行开始是要导入的数据
		for (int i = sheet.getFirstRowNum() + 5; i <= rows; i++) {
			listdata = new ArrayList<String>();
			Row row = sheet.getRow(i);
			if (row != null) {
				//根据字段筛选列数据
				for (FieldBean bean : listf) {
					// 主键不能为空，为空默认空行不插入数据
					if (bean.getCols() == 0 && row.getCell(0) == null) {
						break;
					}
					String str = getStringCellValue(row.getCell(bean.getCols()));
					//单元格为空时为數字字段赋默认值0
					if ("".equals(str) && ("int".equals(bean.getType()) || "long".equals(bean.getType())
							|| "Long".equals(bean.getType()))) {
						str = "0";
					}
					listdata.add(str);
				}
				//过滤空行
				if (listdata.size() > 0) {
					list.add(listdata);
				}
			}

		}

		return list;
	}

	public List<FieldBean> getField(Sheet sheet) {
		List<FieldBean> list = new ArrayList<FieldBean>();
		Row row1 = sheet.getRow(sheet.getFirstRowNum() + 1);
		Row row2 = sheet.getRow(sheet.getFirstRowNum() + 2);

		FieldBean bean = null;
		for (int i = row1.getFirstCellNum(); i < row1.getLastCellNum(); i++) {
			bean = new FieldBean();
			//过滤备注，即没有字段的单元格
			if (!"".equals(getStringCellValue(row1.getCell(i)))) {
				bean.setFieldName(getStringCellValue(row1.getCell(i)));
				bean.setType(getStringCellValue(row2.getCell(i)));
				bean.setCols(i);//记录该字段所在的列
				list.add(bean);
			}
		}

		return list;
	}

	public FieldBean getPrimaryField(Sheet sheet) {
		FieldBean bean = new FieldBean();
		Row row = sheet.getRow(sheet.getFirstRowNum());
		String pstr = getStringCellValue(row.getCell(row.getFirstCellNum()));
		if ("1".equals(pstr)) {
			row = sheet.getRow(sheet.getFirstRowNum() + 1);
			bean.setFieldName(getStringCellValue(row.getCell(row.getFirstCellNum())));
			row = sheet.getRow(sheet.getFirstRowNum() + 2);
			bean.setType(getStringCellValue(row.getCell(row.getFirstCellNum())));
		} else {
			return null;
		}
		return bean;
	}

	@SuppressWarnings("deprecation")
	public String getStringCellValue(Cell cell) {
		String cellValue = "";
		if (cell == null) {
			return "";
		}

		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_STRING:
			cellValue = cell.getStringCellValue();
			break;
		case Cell.CELL_TYPE_NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date date = cell.getDateCellValue();
				cellValue = sdf.format(date);
			} else {
				//格式化數字，不然会出现.0的问题
				NumberFormat nf = NumberFormat.getInstance();
				//它是科学计数法，去掉"，"
				cellValue = nf.format(cell.getNumericCellValue()).replace(",", "");
			}
			break;
		case Cell.CELL_TYPE_BOOLEAN:
			cellValue = String.valueOf(cell.getBooleanCellValue());
			break;
		case Cell.CELL_TYPE_BLANK:
			cellValue = "";
			break;
		default:
			cellValue = "";
			break;
		}
		if (cellValue == "") {
			return "";
		}

		return cellValue.trim();
	}

}
