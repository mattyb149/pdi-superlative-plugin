package org.pentaho.di.trans.steps.superlative;

import java.util.Comparator;

import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;

public class RowValueMetaComparator implements Comparator<Object[]> {

  private ValueMetaInterface valueType;
  private int compareIndex = -1;
  private boolean ascending = true;

  public RowValueMetaComparator( ValueMetaInterface valueType, int compareIndex, boolean ascending ) {
    this();
    if ( valueType != null ) {
      this.valueType = valueType;
    } else {
      throw new IllegalArgumentException( "Value type cannot be null!" );
    }
    if ( compareIndex >= 0 ) {
      this.compareIndex = compareIndex;
    } else {
      throw new IllegalArgumentException( "Comparison index must be >= 0!" );
    }
    this.ascending = ascending;
  }

  public RowValueMetaComparator( ValueMetaInterface valueType, int compareIndex ) {
    this( valueType, compareIndex, true );
  }

  private RowValueMetaComparator() {
  }

  @Override
  public int compare( Object[] o1, Object[] o2 ) {
    int result = 0;
    if ( o1 == null ) {
      if ( o2 == null ) {
        return 0;
      } else {
        return -1;
      }
    } else {
      if ( o2 == null ) {
        return 1;
      }
    }
    try {
      result = ( ascending ? 1 : -1 ) * ( valueType.compare( o1[compareIndex], o2[compareIndex] ) ); // No equals!
    } catch ( KettleValueException e ) {
      throw new IllegalArgumentException( e );
    }
    return result;
  }

}
