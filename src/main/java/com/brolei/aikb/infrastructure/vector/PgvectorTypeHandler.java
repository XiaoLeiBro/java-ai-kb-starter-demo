package com.brolei.aikb.infrastructure.vector;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;

/** 将 float[] 与 pgvector 类型之间转换的 MyBatis TypeHandler. */
public class PgvectorTypeHandler extends BaseTypeHandler<float[]> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, float[] parameter, JdbcType jdbcType)
      throws SQLException {
    PGobject pgObject = new PGobject();
    pgObject.setType("vector");
    StringBuilder sb = new StringBuilder("[");
    for (int j = 0; j < parameter.length; j++) {
      if (j > 0) {
        sb.append(",");
      }
      sb.append(parameter[j]);
    }
    sb.append("]");
    pgObject.setValue(sb.toString());
    ps.setObject(i, pgObject);
  }

  @Override
  public float[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
    String value = rs.getString(columnName);
    return parseVector(value);
  }

  @Override
  public float[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    String value = rs.getString(columnIndex);
    return parseVector(value);
  }

  @Override
  public float[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    String value = cs.getString(columnIndex);
    return parseVector(value);
  }

  private float[] parseVector(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.replaceAll("[\\[\\]]", "").trim();
    if (trimmed.isEmpty()) {
      return new float[0];
    }
    String[] parts = trimmed.split(",");
    float[] result = new float[parts.length];
    for (int i = 0; i < parts.length; i++) {
      result[i] = Float.parseFloat(parts[i].trim());
    }
    return result;
  }
}
