/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * @author mburgess
 * 
 */
@Step( id = "SuperlativeFilter", image = "superlative-filter.png", name = "Top/Bottom/First/Last",
    description = "Filter N rows based on a field or row number", categoryDescription = "Transform" )
public class SuperlativeFilterMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = SuperlativeFilterMeta.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$

  private String valueFieldName = null;

  private long numRowsToSave = 0;

  public FilterType filterType;

  public SuperlativeFilterMeta() {
    super(); // allocate BaseStepMeta
  }

  @Override
  public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    readData( stepnode );
  }

  @Override
  public String getXML() throws KettleException {
    StringBuffer retval = new StringBuffer();
    retval.append( "    " + XMLHandler.addTagValue( "valuefield", getValueFieldName() ) );
    retval.append( "    " + XMLHandler.addTagValue( "rows", getNumRowsToSave() ) );
    retval.append( "    " + XMLHandler.addTagValue( "filter", getFilterType().toString() ) );
    return retval.toString();
  }

  @Override
  public Object clone() {
    SuperlativeFilterMeta retval = (SuperlativeFilterMeta) super.clone();
    retval.setValueFieldName( getValueFieldName() );
    retval.setNumRowsToSave( getNumRowsToSave() );
    retval.setFilterType( getFilterType() );
    return retval;
  }

  private void readData( Node stepnode ) throws KettleXMLException {
    try {
      setValueFieldName( XMLHandler.getTagValue( stepnode, "valuefield" ) );
      int numRows = 0;
      try {
        numRows = Integer.parseInt( XMLHandler.getTagValue( stepnode, "rows" ) );
      } catch ( NumberFormatException nfe ) {
      }
      setNumRowsToSave( numRows );
      setFilterType( FilterType.valueOf( XMLHandler.getTagValue( stepnode, "filter" ) ) );
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages
          .getString( PKG, "SuperlativeFilterMeta.Exception.UnableToReadStepInfo" ), e );
    }
  }

  @Override
  public void setDefault() {
    setValueFieldName( null );
    setNumRowsToSave( -1 );
    setFilterType( FilterType.Top );
  }

  @Override
  public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
    throws KettleException {
    try {
      setValueFieldName( rep.getStepAttributeString( id_step, "valuefield" ) );
      setNumRowsToSave( rep.getStepAttributeInteger( id_step, "rows" ) );
      setFilterType( FilterType.valueOf( rep.getStepAttributeString( id_step, "filter" ) ) );

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG,
          "SuperlativeFilterMeta.Exception.UnexpectedErrorReadingStepInfo" ), e );
    }
  }

  @Override
  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
    throws KettleException {
    try {
      rep.saveStepAttribute( id_transformation, id_step, "valuefield", getValueFieldName() );
      rep.saveStepAttribute( id_transformation, id_step, "rows", getNumRowsToSave() );
      rep.saveStepAttribute( id_transformation, id_step, "filter", getFilterType().toString() );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG,
          "SuperlativeFilterMeta.Exception.UnexpectedErrorSavingStepInfo" ), e );
    }
  }

  @Override
  public void getFields( RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep,
      VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {
    // Default: nothing changes to rowMeta
  }

  @Override
  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
      String input[], String output[], RowMetaInterface info, VariableSpace space, Repository repository,
      IMetaStore metaStore ) {
    CheckResult cr;
    if ( prev == null || prev.size() == 0 ) {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString( PKG,
              "SuperlativeFilterMeta.CheckResult.NotReceivingFields" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
              "SuperlativeFilterMeta.CheckResult.StepRecevingData", prev.size() + "" ), stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString( PKG,
              "SuperlativeFilterMeta.CheckResult.StepRecevingData2" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
          new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
              "SuperlativeFilterMeta.CheckResult.NoInputReceivedFromOtherSteps" ), stepMeta );
      remarks.add( cr );
    }
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr,
      Trans trans ) {
    return new SuperlativeFilter( stepMeta, stepDataInterface, cnr, tr, trans );
  }

  @Override
  public StepDataInterface getStepData() {
    return new SuperlativeFilterData();
  }

  public String getValueFieldName() {
    return valueFieldName;
  }

  public void setValueFieldName( String valueFieldName ) {
    this.valueFieldName = valueFieldName;
  }

  public long getNumRowsToSave() {
    return numRowsToSave;
  }

  public void setNumRowsToSave( long l ) {
    this.numRowsToSave = l;
  }

  public FilterType getFilterType() {
    return filterType;
  }

  public void setFilterType( FilterType filterType ) {
    this.filterType = filterType;
  }
}
