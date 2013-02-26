package org.sagebionetworks.repo.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.sagebionetworks.repo.model.AuthorizationConstants;
import org.sagebionetworks.repo.model.DatastoreException;
import org.sagebionetworks.repo.model.PaginatedResults;
import org.sagebionetworks.repo.model.ServiceConstants;
import org.sagebionetworks.repo.model.TrashedEntity;
import org.sagebionetworks.repo.model.UnauthorizedException;
import org.sagebionetworks.repo.web.NotFoundException;
import org.sagebionetworks.repo.web.UrlHelpers;
import org.sagebionetworks.repo.web.service.ServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * REST APIs for the trash can.
 *
 * @author Eric Wu
 */
@Controller
public class TrashController extends BaseController {

	@Autowired
	private ServiceProvider serviceProvider;

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = {UrlHelpers.TRASHCAN_TRASH}, method = RequestMethod.PUT)
	public void moveToTrash(
			@RequestParam(value = AuthorizationConstants.USER_ID_PARAM, required = false) String userId,
			@PathVariable String id)
			throws NotFoundException, DatastoreException, UnauthorizedException {
		this.serviceProvider.getTrashService().moveToTrash(userId, id);
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = {UrlHelpers.TRASHCAN_RESTORE}, method = RequestMethod.PUT)
	public void restoreFromTrash(
			@RequestParam(value = AuthorizationConstants.USER_ID_PARAM, required = false) String userId,
			@PathVariable String id)
			throws NotFoundException, DatastoreException, UnauthorizedException {
		this.serviceProvider.getTrashService().restoreFromTrash(userId, id, null);
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = {UrlHelpers.TRASHCAN_RESTORE_TO_PARENT}, method = RequestMethod.PUT)
	public void restoreFromTrash(
			@RequestParam(value = AuthorizationConstants.USER_ID_PARAM, required = false) String userId,
			@PathVariable String id,
			@PathVariable String parentId)
			throws NotFoundException, DatastoreException, UnauthorizedException {
		this.serviceProvider.getTrashService().restoreFromTrash(userId, id, parentId);
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = {UrlHelpers.TRASHCAN_VIEW}, method = RequestMethod.GET)
	public @ResponseBody PaginatedResults<TrashedEntity> viewTrash(
	        @RequestParam(value = AuthorizationConstants.USER_ID_PARAM, required = false) String userId,
			@RequestParam(value = ServiceConstants.PAGINATION_OFFSET_PARAM, required = false, defaultValue = ServiceConstants.DEFAULT_PAGINATION_OFFSET_PARAM_NEW) Long offset,
			@RequestParam(value = ServiceConstants.PAGINATION_LIMIT_PARAM, required = false, defaultValue = ServiceConstants.DEFAULT_PAGINATION_LIMIT_PARAM) Long limit,
			HttpServletRequest request) throws DatastoreException, NotFoundException {
		return serviceProvider.getTrashService().viewTrash(userId, offset, limit, request);
	}
}