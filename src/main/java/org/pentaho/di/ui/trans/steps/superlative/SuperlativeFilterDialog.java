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

package org.pentaho.di.ui.trans.steps.superlative;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.superlative.FilterType;
import org.pentaho.di.trans.steps.superlative.SuperlativeFilterMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

public class SuperlativeFilterDialog extends BaseStepDialog implements StepDialogInterface {
  private static Class<?> PKG = SuperlativeFilterMeta.class; // for i18n purposes, needed by Translator2!! $NON-NLS-1$

  private SuperlativeFilterMeta input;

  private boolean gotPreviousFields = false;
  private RowMetaInterface previousFields;

  private CCombo wValueField;

  private Label wlNumRowsToSave;
  private TextVar wNumRowsToSave;
  private FormData fdlNumRowsToSave, fdNumRowsToSave;

  private Label wlTopBottomFirstLast;
  private Button radioTop, radioBottom, radioFirst, radioLast;
  private FormData fdlTopBottomFirstLast, fdTopBottomFirstLast;

  public SuperlativeFilterDialog( Shell parent, Object in, TransMeta tr, String sname ) {
    super( parent, (BaseStepMeta) in, tr, sname );
    input = (SuperlativeFilterMeta) in;
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX );
    props.setLook( shell );
    setShellImage( shell, input );

    ModifyListener lsMod = new ModifyListener() {
      public void modifyText( ModifyEvent e ) {
        input.setChanged();
      }
    };
    changed = input.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "SuperlativeFilterDialog.Shell.Title" ) );

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;

    // Stepname line
    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "SuperlativeFilterDialog.Stepname.Label" ) );
    props.setLook( wlStepname );
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment( 0, 0 );
    fdlStepname.right = new FormAttachment( middle, -margin );
    fdlStepname.top = new FormAttachment( 0, margin );
    wlStepname.setLayoutData( fdlStepname );
    wStepname = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wStepname.setText( stepname );
    props.setLook( wStepname );
    wStepname.addModifyListener( lsMod );
    fdStepname = new FormData();
    fdStepname.left = new FormAttachment( middle, 0 );
    fdStepname.top = new FormAttachment( 0, margin );
    fdStepname.right = new FormAttachment( 100, 0 );
    wStepname.setLayoutData( fdStepname );

    // Select input field
    Label wlInField = new Label( shell, SWT.RIGHT );
    wlInField.setText( BaseMessages.getString( PKG, "SuperlativeFilterDialog.ValueField.Label" ) );
    props.setLook( wlInField );
    FormData fdlTrueTo = new FormData();
    fdlTrueTo.left = new FormAttachment( 0, 0 );
    fdlTrueTo.right = new FormAttachment( middle, -margin );
    fdlTrueTo.top = new FormAttachment( wStepname, margin );
    wlInField.setLayoutData( fdlTrueTo );
    wValueField = new CCombo( shell, SWT.BORDER );
    props.setLook( wValueField );
    wValueField.addModifyListener( lsMod );
    FormData fdInField = new FormData();
    fdInField.left = new FormAttachment( middle, 0 );
    fdInField.top = new FormAttachment( wStepname, margin );
    fdInField.right = new FormAttachment( 100, 0 );
    wValueField.setLayoutData( fdInField );
    wValueField.addFocusListener( new FocusListener() {
      public void focusLost( org.eclipse.swt.events.FocusEvent e ) {
      }

      public void focusGained( org.eclipse.swt.events.FocusEvent e ) {
        Cursor busy = new Cursor( shell.getDisplay(), SWT.CURSOR_WAIT );
        shell.setCursor( busy );
        getFieldsInto( wValueField );
        shell.setCursor( null );
        busy.dispose();
      }
    } );

    // Number of Rows to Save field
    wlNumRowsToSave = new Label( shell, SWT.RIGHT );
    wlNumRowsToSave.setText( BaseMessages.getString( PKG, "SuperlativeFilterDialog.NumRowsToSave.Label" ) );
    props.setLook( wlNumRowsToSave );
    fdlNumRowsToSave = new FormData();
    fdlNumRowsToSave.left = new FormAttachment( 0, 0 );
    fdlNumRowsToSave.right = new FormAttachment( middle, -margin );
    fdlNumRowsToSave.top = new FormAttachment( wValueField, margin );
    wlNumRowsToSave.setLayoutData( fdlNumRowsToSave );
    wNumRowsToSave = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wNumRowsToSave.setText( "" );
    props.setLook( wNumRowsToSave );
    wNumRowsToSave.addModifyListener( lsMod );
    fdNumRowsToSave = new FormData();
    fdNumRowsToSave.left = new FormAttachment( middle, 0 );
    fdNumRowsToSave.top = new FormAttachment( wValueField, margin );
    fdNumRowsToSave.right = new FormAttachment( 100, 0 );
    wNumRowsToSave.setLayoutData( fdNumRowsToSave );

    // Top/Bottom choice
    wlTopBottomFirstLast = new Label( shell, SWT.RIGHT );
    wlTopBottomFirstLast.setText( BaseMessages.getString( PKG, "SuperlativeFilterDialog.TopBottomFirstLast.Label" ) );
    props.setLook( wlTopBottomFirstLast );
    fdlTopBottomFirstLast = new FormData();
    fdlTopBottomFirstLast.left = new FormAttachment( 0, 0 );
    fdlTopBottomFirstLast.right = new FormAttachment( middle, -margin );
    fdlTopBottomFirstLast.top = new FormAttachment( wNumRowsToSave, margin );
    wlTopBottomFirstLast.setLayoutData( fdlTopBottomFirstLast );

    // The TOP option...
    //
    Group gTopBottom = new Group( shell, SWT.SHADOW_ETCHED_IN );
    props.setLook( gTopBottom );
    // The layout
    FormLayout localLayout = new FormLayout();
    localLayout.marginWidth = Const.FORM_MARGIN;
    localLayout.marginHeight = Const.FORM_MARGIN;
    gTopBottom.setLayout( localLayout );
    FormData fdRadioGroup = new FormData();
    fdRadioGroup.top = new FormAttachment( wNumRowsToSave, margin );
    fdRadioGroup.left = new FormAttachment( middle, 0 );
    fdRadioGroup.right = new FormAttachment( 100, 0 );
    gTopBottom.setLayoutData( fdRadioGroup );

    radioTop = new Button( gTopBottom, SWT.RADIO );
    props.setLook( radioTop );
    radioTop.setText( BaseMessages.getString( PKG, "SuperlativeFilterDialog.TopButton.Label" ) );
    FormData fdRadioTop = new FormData();
    fdRadioTop.top = new FormAttachment( 0, 0 );
    fdRadioTop.left = new FormAttachment( 0, 0 );
    fdRadioTop.right = new FormAttachment( 25, 0 );
    radioTop.setLayoutData( fdRadioTop );
    radioTop.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        input.setChanged( true );
      }
    } );

    // The BOTTOM option...
    //
    radioBottom = new Button( gTopBottom, SWT.RADIO );
    props.setLook( radioBottom );
    radioBottom.setText( BaseMessages.getString( PKG, "SuperlativeFilterDialog.BottomButton.Label" ) );
    FormData fdRadioBottom = new FormData();
    fdRadioBottom.top = new FormAttachment( 0, 0 );
    fdRadioBottom.left = new FormAttachment( 25, 0 );
    fdRadioBottom.right = new FormAttachment( 50, 0 );
    radioBottom.setLayoutData( fdRadioBottom );
    radioBottom.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        input.setChanged( true );
      }
    } );

    // The FIRST option...
    //
    radioFirst = new Button( gTopBottom, SWT.RADIO );
    props.setLook( radioFirst );
    radioFirst.setText( BaseMessages.getString( PKG, "SuperlativeFilterDialog.FirstButton.Label" ) );
    FormData fdRadioFirst = new FormData();
    fdRadioFirst.top = new FormAttachment( 0, 0 );
    fdRadioFirst.left = new FormAttachment( 50, 0 );
    fdRadioFirst.right = new FormAttachment( 75, 0 );
    radioFirst.setLayoutData( fdRadioFirst );
    radioFirst.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        input.setChanged( true );
      }
    } );

    // The LAST option...
    //
    radioLast = new Button( gTopBottom, SWT.RADIO );
    props.setLook( radioLast );
    radioLast.setText( BaseMessages.getString( PKG, "SuperlativeFilterDialog.LastButton.Label" ) );
    FormData fdRadioLast = new FormData();
    fdRadioLast.top = new FormAttachment( 0, 0 );
    fdRadioLast.left = new FormAttachment( 75, 0 );
    fdRadioLast.right = new FormAttachment( 100, 0 );
    radioLast.setLayoutData( fdRadioLast );
    radioLast.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent arg0 ) {
        input.setChanged( true );
      }
    } );

    // Some buttons
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    positionBottomButtons( shell, new Button[] { wOK, wCancel }, margin, null );

    // Add listeners
    lsCancel = new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    };
    lsOK = new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    };

    wCancel.addListener( SWT.Selection, lsCancel );
    wOK.addListener( SWT.Selection, lsOK );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    wStepname.addSelectionListener( lsDef );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    // Set the shell size, based upon previous time...
    setSize();

    getData();
    input.setChanged( changed );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() )
        display.sleep();
    }
    return stepname;
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {

    if ( !Const.isEmpty( input.getValueFieldName() ) ) {
      wValueField.setText( input.getValueFieldName() );
    }

    wNumRowsToSave.setText( Long.toString( Math.max( input.getNumRowsToSave(), 0 ) ) );

    FilterType filterType = input.getFilterType();

    radioTop.setSelection( FilterType.Top.equals( filterType ) );
    radioBottom.setSelection( FilterType.Bottom.equals( filterType ) );
    radioFirst.setSelection( FilterType.First.equals( filterType ) );
    radioLast.setSelection( FilterType.Last.equals( filterType ) );

    wStepname.selectAll();
    wStepname.setFocus();
  }

  private void cancel() {
    stepname = null;
    input.setChanged( changed );
    dispose();
  }

  private void ok() {
    if ( Const.isEmpty( wStepname.getText() ) )
      return;

    stepname = wStepname.getText(); // return value
    input.setValueFieldName( wValueField.getText() );
    input.setNumRowsToSave( Integer.parseInt( wNumRowsToSave.getText() ) );

    if ( radioBottom.getSelection() ) {
      input.setFilterType( FilterType.Bottom );
    } else if ( radioFirst.getSelection() ) {
      input.setFilterType( FilterType.First );
    } else if ( radioLast.getSelection() ) {
      input.setFilterType( FilterType.Last );
    } else {
      // Default to Top
      input.setFilterType( FilterType.Top );
    }
    dispose();
  }

  private void getFieldsInto( CCombo fieldCombo ) {
    try {
      if ( !gotPreviousFields ) {
        previousFields = transMeta.getPrevStepFields( stepname );
      }

      String field = fieldCombo.getText();

      if ( previousFields != null ) {
        fieldCombo.setItems( previousFields.getFieldNames() );
      }

      if ( field != null ) {
        fieldCombo.setText( field );
      }
      gotPreviousFields = true;

    } catch ( KettleException ke ) {
      new ErrorDialog( shell, BaseMessages.getString( PKG, "SuperlativeFilterDialog.FailedToGetFields.DialogTitle" ),
          BaseMessages.getString( PKG, "SuperlativeFilterDialog.FailedToGetFields.DialogMessage" ), ke );
    }
  }
}
