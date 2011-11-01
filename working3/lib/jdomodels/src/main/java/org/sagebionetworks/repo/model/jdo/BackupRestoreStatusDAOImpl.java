package org.sagebionetworks.repo.model.jdo;

import org.sagebionetworks.ids.IdGenerator;
import org.sagebionetworks.repo.model.BackupRestoreStatus;
import org.sagebionetworks.repo.model.BackupRestoreStatusDAO;
import org.sagebionetworks.repo.model.DatastoreException;
import org.sagebionetworks.repo.model.jdo.persistence.JDOBackupRestoreStatus;
import org.sagebionetworks.repo.model.jdo.persistence.JDOBackupTerminate;
import org.sagebionetworks.repo.web.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.jdo.JdoObjectRetrievalFailureException;
import org.springframework.orm.jdo.JdoTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the BackupRestoreStatusDAO. Note: Since a
 * BackupRestoreStatus is used to track the progress of a daemon, and all
 * updates will come from the daemon, the updates must occur in a new
 * transaction separate from the daemon's transactions.
 * 
 * @author jmhill
 * 
 */
@Transactional(readOnly = true)
public class BackupRestoreStatusDAOImpl implements BackupRestoreStatusDAO {

	@Autowired
	private JdoTemplate jdoTemplate;

	@Autowired
	private IdGenerator idGenerator;

	/**
	 * Create a new status object.
	 * 
	 * Note: Requires a new Transaction. Since a BackupRestoreStatus is used to
	 * track the progress of a daemon, and all updates will come from the
	 * daemon, the updates must occur in a new transaction separate from the
	 * daemon's transactions.
	 * 
	 * @return The ID of the newly created status.
	 * @throws DatastoreException
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	@Override
	public String create(BackupRestoreStatus dto) throws DatastoreException {
		// First assign the id
		// Create a new jdo
		JDOBackupRestoreStatus jdo = new JDOBackupRestoreStatus();
		BackupRestoreStatusUtil.updateJdoFromDto(dto, jdo);
		// Since we will use the ID in the backup file URL we want it to be
		// unique within the domain.
		jdo.setId(idGenerator.generateNewId());
		jdoTemplate.makePersistent(jdo);
		JDOBackupTerminate terminateJdo = new JDOBackupTerminate();
		terminateJdo.setOwner(jdo);
		terminateJdo.setForceTerminate(false);
		jdoTemplate.makePersistent(terminateJdo);
		return KeyFactory.keyToString(jdo.getId());
	}

	/**
	 * Get a status object from its id.
	 * 
	 * @throws DatastoreException
	 * @throws NotFoundException
	 * @throws DataAccessException
	 */
	@Transactional(readOnly = true)
	@Override
	public BackupRestoreStatus get(String id) throws DatastoreException,
			NotFoundException {
		JDOBackupRestoreStatus jdo = getJdo(id);
		return BackupRestoreStatusUtil.createDtoFromJdo(jdo);
	}

	/**
	 * Get the JDO object for this id.
	 * 
	 * @param id
	 * @return
	 * @throws DatastoreException
	 * @throws NotFoundException
	 */
	private JDOBackupRestoreStatus getJdo(String id) throws DatastoreException,
			NotFoundException {
		try {
			return jdoTemplate.getObjectById(JDOBackupRestoreStatus.class,
					KeyFactory.stringToKey(id));
		} catch (JdoObjectRetrievalFailureException e) {
			throw new NotFoundException(
					"Cannot find a BackupRestoreStatus with ID: " + id);
		}
	}

	/**
	 * Update a status object. Concurrency is not an issue here since only the
	 * daemon that created it will update it.
	 * 
	 * Note: Requires a new Transaction. Since a BackupRestoreStatus is used to
	 * track the progress of a daemon, and all updates will come from the
	 * daemon, the updates must occur in a new transaction separate from the
	 * daemon's transactions.
	 * 
	 * @throws NotFoundException
	 * @throws DatastoreException
	 * 
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	@Override
	public void update(BackupRestoreStatus dto) throws DatastoreException,
			NotFoundException {
		if (dto == null)
			throw new IllegalArgumentException("Status cannot be null");
		if (dto.getId() == null)
			throw new IllegalArgumentException("Status.id cannot be null");
		JDOBackupRestoreStatus jdo = getJdo(dto.getId());
		BackupRestoreStatusUtil.updateJdoFromDto(dto, jdo);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	@Override
	public void delete(String id) throws DatastoreException, NotFoundException {
		JDOBackupRestoreStatus jdo = getJdo(id);
		jdoTemplate.deletePersistent(jdo);
	}

	private JDOBackupTerminate getJobTerminate(String id)
			throws DataAccessException, DatastoreException, NotFoundException {
		try {
			JDOBackupRestoreStatus status = getJdo(id);
			return jdoTemplate.getObjectById(JDOBackupTerminate.class,status);
		} catch (JdoObjectRetrievalFailureException e) {
			throw new NotFoundException(
					"Cannot find a BackupRestoreStatus with ID: " + id);
		}

	}

	@Override
	public boolean shouldJobTerminate(String id) throws DatastoreException, NotFoundException {
		JDOBackupTerminate terminateJdo = getJobTerminate(id);
		return terminateJdo.getForceTerminate();
	}

	/**
	 * Note: Requires a new Transaction. Value changes will occur through web services
	 * while the value will be checked from the backup daemon.
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	@Override
	public void setForceTermination(String id, boolean terminate)
			throws DatastoreException, NotFoundException {
		JDOBackupTerminate terminateJdo = getJobTerminate(id);
		terminateJdo.setForceTerminate(terminate);
	}
}
