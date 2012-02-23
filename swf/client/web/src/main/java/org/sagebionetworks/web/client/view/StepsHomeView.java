package org.sagebionetworks.web.client.view;

import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface StepsHomeView extends IsWidget, SynapseView {
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	/**
	 * Sets the current user id in the view
	 * 
	 * @param userId
	 */
	public void setCurrentUserId(String userId);
		
	public interface Presenter {

		PlaceChanger getPlaceChanger();
	}

}
