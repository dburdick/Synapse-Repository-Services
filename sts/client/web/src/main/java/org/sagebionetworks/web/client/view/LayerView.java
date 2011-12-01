package org.sagebionetworks.web.client.view;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapsePresenter;
import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.shared.FileDownload;
import org.sagebionetworks.web.shared.LicenseAgreement;
import org.sagebionetworks.web.shared.TableResults;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Defines the communication between the view and presenter for a view of a single datasets.
 * 
 * @author jmhill
 *
 */
public interface LayerView extends IsWidget, SynapseView {
	
	/**
	 * This how the view communicates with the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
		
	/**
	 * Shows a message if the preview doesn't exist or is not available
	 */
	public void showLayerPreviewUnavailable();	
	
	/**
	 * Removes the download button
	 */
	public void setDownloadUnavailable();
	
	/**
	 * Sets the values to display in the view
	 * @param processingFacility
	 * @param qcByDisplay
	 * @param qcByUrl
	 * @param qcAnalysisDisplay
	 * @param qcAnalysisUrl
	 * @param qcDate
	 * @param overviewText
	 * @param nDataRowsShown
	 * @param totalDataRows
	 * @param privacyLevel
	 * @param layerType 
	 */
	public void setLayerDetails(String id,
								String layerName,								
								String processingFacility, 
								String qcByDisplay,
								String qcByUrl, 
								String qcAnalysisDisplay, 
								String qcAnalysisUrl,
								Date qcDate, 
								String overviewText, 
								int nDataRowsShown,
								int totalDataRows, 
								String privacyLevel,
								String datasetLink,
								String platform, 
								boolean isAdministrator, 
								boolean canEdit, String layerType);
	
	/**
	 * require the view to show the license agreement
	 * @param requireLicense
	 */
	public void requireLicenseAcceptance(boolean requireLicense);
	
	/**
	 * the license agreement to be shown
	 * @param agreement
	 */
	public void setLicenseAgreement(LicenseAgreement agreement);
	
	/**
	 * Set the list of files available via the whole dataset download
	 * @param downloads
	 */
	public void setLicensedDownloads(List<FileDownload> downloads);
	
	/**
	 * Disables the downloading of files
	 * @param disable
	 */
	public void disableLicensedDownloads(boolean disable);

	
	/**
	 * Shows a download modal for this layer
	 */
	public void showDownload();
	
	/**
	 * Tells the view that the downloads are loading
	 */
	public void showDownloadsLoading();

	
	/**
	 * This sets the data to be shown in the preview table
	 * @param preview
	 * @param columnDisplayOrder
	 * @param columnUnits 
	 * @param columnDescriptions 
	 */
	public void setLayerPreviewTable(List<Map<String,String>> rows, List<String> columnDisplayOrder, Map<String, String> columnDescriptions, Map<String, String> columnUnits);
	
	/**
	 * Defines the communication with the presenter.
	 *
	 */
	public interface Presenter extends SynapsePresenter {

		/**
		 * Refreshes the object on the page
		 */
		public void refresh();

		/**
		 * called when the user has accepted the license in the view
		 */
		public void licenseAccepted();

		/**
		 * Determine if a download screen can be displayed to the user
		 * @return true if the user should be shown the download screen 
		 */
		public boolean downloadAttempted();

		/**
		 * Deletes this Layer
		 */
		public void delete();

		/**
		 * Changes to the view to the pheno type editor
		 */
		public void openPhenoTypeEditor();
				
	}

}
