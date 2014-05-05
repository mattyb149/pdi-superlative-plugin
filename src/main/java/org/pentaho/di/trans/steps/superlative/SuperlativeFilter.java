/*******************************************************************************
 *
 * Matt Burgess
 * Copyright (C) 2014 by Matt Burgess
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.superlative;

import java.util.LinkedList;
import java.util.Queue;

import com.google.common.collect.MinMaxPriorityQueue;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * This step returns the top or bottom N rows based on the values of a specified field.
 * 
 */
public class SuperlativeFilter extends BaseStep implements StepInterface {
  private static Class<?> PKG = SuperlativeFilterMeta.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$

  private Queue<Object[]> rowQ;
  private SuperlativeFilterMeta meta;
  private ValueMetaInterface valueFieldType;
  private int valueIndex = -1;

  private RowMetaInterface outputRowMeta;

  private RowValueMetaComparator rowComparator = null;

  private boolean topFilter, bottomFilter, firstFilter, lastFilter;

  public SuperlativeFilter( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    if ( !super.init( smi, sdi ) ) {
      return false;
    }

    meta = (SuperlativeFilterMeta) smi;

    return true;
  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {

    Object[] r = getRow(); // get row, set busy!

    if ( first ) {
      outputRowMeta = getInputRowMeta().clone();
      valueIndex = outputRowMeta.indexOfValue( meta.getValueFieldName() );
      if ( valueIndex < 0 && ( topFilter || bottomFilter ) ) {
        throw new KettleException( BaseMessages.getString( PKG, "SuperlativeFilter.Error.NoValueField", meta
            .getValueFieldName() ) );
      }
      valueFieldType = outputRowMeta.getValueMeta( valueIndex );
      FilterType metaFilterType = meta.getFilterType();
      topFilter = FilterType.Top.equals( metaFilterType );
      bottomFilter = FilterType.Bottom.equals( metaFilterType );
      firstFilter = FilterType.First.equals( metaFilterType );
      lastFilter = FilterType.Last.equals( metaFilterType );

      if ( topFilter || bottomFilter ) {
        rowComparator = new RowValueMetaComparator( valueFieldType, valueIndex, topFilter );
        rowQ = MinMaxPriorityQueue.orderedBy( rowComparator ).expectedSize( (int) meta.getNumRowsToSave() ).create();
      } else if ( firstFilter || lastFilter ) {
        rowQ = new LinkedList<Object[]>();
      } else
        throw new KettleException( "Meta filter type = " + metaFilterType );

      first = false;
    }

    if ( r == null ) {
      // no more input to be expected...
      return putQueue( topFilter || bottomFilter );
    }

    // Determine whether to save row
    if ( ( rowQ.size() < meta.getNumRowsToSave() ) || firstFilter || lastFilter ) {
      rowQ.add( r );
    } else {
      Object[] head = rowQ.peek();

      if ( rowComparator.compare( r, head ) >= 0 ) {
        rowQ.add( r );
      }
    }

    // If we're returning the first N rows and we have N rows, we're done!
    if ( firstFilter && ( rowQ.size() == meta.getNumRowsToSave() ) ) {
      return putQueue( false );
    }

    if ( rowQ.size() > meta.getNumRowsToSave() ) {
      rowQ.poll();
    }

    if ( checkFeedback( getLinesRead() ) ) {
      if ( log.isBasic() )
        logBasic( BaseMessages.getString( PKG, "SuperlativeFilter.Log.LineNumber" ) + getLinesRead() );
    }

    return true;
  }

  private boolean putQueue( boolean pollLast ) throws KettleStepException {
    if ( rowQ != null ) {
      // Can't use iterator here (not guaranteed to be in priority order),
      // plus they might be in reverse order, so reorder and put out the rows
      while ( !rowQ.isEmpty() ) {
        putRow( outputRowMeta, ( pollLast && rowQ instanceof MinMaxPriorityQueue )
            ? ( (MinMaxPriorityQueue<Object[]>) rowQ ).pollLast() : rowQ.poll() );
      }
    }
    setOutputDone();
    return false;
  }
}
