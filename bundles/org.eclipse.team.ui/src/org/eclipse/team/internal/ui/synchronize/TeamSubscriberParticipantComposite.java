/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.action.*;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.jobs.IJobListener;
import org.eclipse.team.internal.ui.jobs.JobStatusHandler;
import org.eclipse.team.ui.controls.IControlFactory;
import org.eclipse.team.ui.synchronize.ISynchronizeView;
import org.eclipse.team.ui.synchronize.TeamSubscriberParticipant;
import org.eclipse.team.ui.synchronize.actions.SubscriberAction;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class TeamSubscriberParticipantComposite extends Composite implements IPropertyChangeListener {
	private TeamSubscriberParticipant participant;	
	private Color background;
	private ISynchronizeView view;
	private IControlFactory factory;
	
	private Label lastSyncLabel;
	private Label scheduleLabel;
	private Label statusLabel;
	private TableViewer rootsList;
	
	private IJobListener jobListener = new IJobListener() {
		public void started(QualifiedName jobType) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					synchronized (this) {
						if(statusLabel != null && !statusLabel.isDisposed())
							statusLabel.setText("Working...");
					}
				}
			});
		}
		public void finished(QualifiedName jobType) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					synchronized (this) {
						if(statusLabel != null && !statusLabel.isDisposed())
							statusLabel.setText("Idle");
					}
				}
			});

		}
	};
	
	public TeamSubscriberParticipantComposite(Composite parent, IControlFactory factory, TeamSubscriberParticipant participant, ISynchronizeView view) {
		super(parent, SWT.NONE);
		this.factory = factory;
		this.participant = participant;		
		this.view = view;
		createComposite(this);
		updateLastRefreshLabel();
		updateStatusLabel();
		participant.addPropertyChangeListener(this);
		
		JobStatusHandler.addJobListener(jobListener, SubscriberAction.SUBSCRIBER_JOB_TYPE);
		if(JobStatusHandler.hasRunningJobs(SubscriberAction.SUBSCRIBER_JOB_TYPE)) {
			statusLabel.setText("Working...");
		} else {
			statusLabel.setText("Idle");
		}
	}
	
	protected Composite createComposite(Composite area) {
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		area.setLayout(layout);
		setBackground(factory.getBackgroundColor());
		{
			final Composite composite_1 = factory.createComposite(area, SWT.NONE);
			GridData gridData = new GridData(GridData.VERTICAL_ALIGN_FILL);
			final GridLayout gridLayout_1 = new GridLayout();
			gridLayout_1.numColumns = 2;
			gridLayout_1.marginHeight = 0;
			gridLayout_1.marginWidth = 0;
			composite_1.setLayout(gridLayout_1);
			composite_1.setLayoutData(gridData);
			{
				final Label label = factory.createLabel(composite_1, "Last Sync");
				gridData = new GridData();
				label.setLayoutData(gridData);
			}
			{
				lastSyncLabel = factory.createLabel(composite_1, "11/23/03 10:03:12");
				gridData = new GridData();
				gridData.grabExcessHorizontalSpace = true;
				lastSyncLabel.setLayoutData(gridData);
			}
			{
				final Label label = factory.createLabel(composite_1, "Schedule");
				gridData = new GridData();
				label.setLayoutData(gridData);
			}
			{
				scheduleLabel = factory.createLabel(composite_1, "Every Hour");
				gridData = new GridData();
				gridData.grabExcessHorizontalSpace = true;
				scheduleLabel.setLayoutData(gridData);
			}
			{
				final Label label = factory.createLabel(composite_1, "Status");
				gridData = new GridData();
				label.setLayoutData(gridData);
			}
			{
				statusLabel = factory.createLabel(composite_1, "Idle");
				gridData = new GridData();
				gridData.grabExcessHorizontalSpace = true;
				statusLabel.setLayoutData(gridData);
			}
		}
		{
			final Composite composite_1 = factory.createComposite(area, SWT.NONE);
			GridData gridData = new GridData(GridData.FILL_BOTH);
			final GridLayout gridLayout_1 = new GridLayout();
			gridLayout_1.numColumns = 1;
			gridLayout_1.marginHeight = 0;
			gridLayout_1.marginWidth = 0;
			composite_1.setLayout(gridLayout_1);
			composite_1.setLayoutData(gridData);
			{
				rootsList = new TableViewer(composite_1, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);				
				gridData = new GridData(GridData.FILL_BOTH);
				gridData.heightHint = 80;
				rootsList.getTable().setLayoutData(gridData);	
				rootsList.setLabelProvider(new WorkbenchLabelProvider());
				rootsList.setContentProvider(new ArrayContentProvider());
				rootsList.setInput(participant.getInput().subscriberRoots());
				factory.paintBordersFor(rootsList.getTable());
				hookContextMenu();
			}
		}		
		return area;
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	public void dispose() {
		super.dispose();
		participant.removePropertyChangeListener(this);
		JobStatusHandler.removeJobListener(jobListener, SubscriberAction.SUBSCRIBER_JOB_TYPE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		if(! isDisposed()) {
			String property = event.getProperty();
			if(property.equals(TeamSubscriberParticipant.P_SYNCVIEWPAGE_LASTSYNC)) {
				updateLastRefreshLabel();
			} else if(property.equals(TeamSubscriberParticipant.P_SYNCVIEWPAGE_STATUS)) {
				updateStatusLabel();
			}
			layout(true);
		}
	}
	
	private void updateStatusLabel() {
		statusLabel.setText(participant.getStatusText());
	}

	private void updateLastRefreshLabel() {
		long mills = participant.getLastRefreshTime();
		final String text;
		if(mills <= 0) {
			text = Policy.bind("SyncViewPreferencePage.lastRefreshRunNever"); //$NON-NLS-1$
		} else {
			Date lastTimeRun = new Date(mills);
			text = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(lastTimeRun);
		}
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				lastSyncLabel.setText(text);
			}
		});
	}
	
	protected void hookContextMenu() {
		if(rootsList != null) {
			final MenuManager menuMgr = new MenuManager(participant.getId()); //$NON-NLS-1$
			menuMgr.setRemoveAllWhenShown(true);
			menuMgr.addMenuListener(new IMenuListener() {
				public void menuAboutToShow(IMenuManager manager) {
					setContextMenu(manager);
				}
			});
			Menu menu = menuMgr.createContextMenu(rootsList.getControl());			
			rootsList.getControl().setMenu(menu);			
			view.getSite().registerContextMenu(participant.getId(), menuMgr, rootsList);
		}
	}
	
	protected void setContextMenu(IMenuManager manager) {	
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
}