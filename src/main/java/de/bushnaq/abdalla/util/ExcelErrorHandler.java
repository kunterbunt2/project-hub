package de.bushnaq.abdalla.util;


/**
 * We use following intermediate data structures
 * A map of dependencies mapped to the row number
 * A map of tasks mapped to the row number
 * A map of tasks mapped to category names
 * A map of resources mapped to their names
 *
 * @author abdalla.bushnaq
 */
public class ExcelErrorHandler extends ErrorHandler {

//    private ColumnHeaderList columnHeaderList = null;
//
//    //    @Override
//    public void addComment(String message) {
//        addComment(message, null, 0);
//    }
//
//    public void addComment(String message, Row row, Integer excelColumnIndex) {
//        exceptions.add(new Xlsx2mppException(message));
//        if (row != null && excelColumnIndex != null) {
//            Cell cell = row.getCell(excelColumnIndex);
//            if (cell == null) {
//                cell = row.createCell(excelColumnIndex, CellType.BLANK);
//            }
//            ExcelUtil.addComment(cell, message);
//        }
//    }
//
//    private void addComment(String message, Row row, String columnName) {
//        int excelColumnIndex = columnHeaderList.getExcelColumnIndex(columnName);
//        addComment(message, row, excelColumnIndex);
//    }
//
//    public void init(ColumnHeaderList columnHeaderList) {
//        this.columnHeaderList = columnHeaderList;
//    }
//
//    public void isEqual(String message, Row row, String columnName, Double d1, Double d2, Double delta) {
//        if (doubleIsDifferent(d1, d2, delta)) {
//            noException = false;
//            addComment(message, row, columnName);
//        }
//    }
//
//    public void isEqual(String message, Row row, String columnName, Object d1, Object d2) {
//        if (!d1.equals(d2)) {
//            noException = false;
//            addComment(message, row, columnName);
//        }
//    }
//
//    public boolean isFalse(String message, Row row, Integer columnIndex, boolean value) {
//        if (value) {
//            noException = false;
//            addComment(message, row, columnIndex);
//            return false;
//        }
//        return true;
//    }
//
//    public void isNotEqual(String message, Row row, String columnName, Object o1, Object o2) {
//        if (o1 != null && o1.equals(o2)) {
//            noException = false;
//            addComment(message, row, columnName);
//        }
//    }
//
//    public boolean isNotNull(String message, Object o) {
//        if (o == null) {
//            noException = false;
//            addComment(message);
//            return false;
//        }
//        return true;
//    }
//
//    public boolean isNotNull(String message, Row row, Integer columnIndex, Object o) {
//        if (o == null) {
//            noException = false;
//            addComment(message, row, columnIndex);
//            return false;
//        }
//        return true;
//    }
//
//    public boolean isNotNull(String message, Row row, String columnName, Object o) {
//        if (o == null) {
//            noException = false;
//            addComment(message, row, columnName);
//            return false;
//        }
//        return true;
//    }
//
//    public void isNull(String message, Row row, String columnName, Object o) {
//        if (o != null) {
//            noException = false;
//            addComment(message, row, columnName);
//        }
//    }
//
//    public boolean isTrue(String message, Row row, Integer columnIndex, boolean value) {
//        if (!value) {
//            noException = false;
//            addComment(message, row, columnIndex);
//            return false;
//        }
//        return true;
//    }
//
//    public void isTrue(String message, Row row, String columnName, boolean value) {
//        if (!value) {
//            noException = false;
//            addComment(message, row, columnName);
//        }
//    }

}
