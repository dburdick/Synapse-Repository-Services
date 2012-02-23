package org.sagebionetworks.repo.model.dbo.persistence;

import static org.sagebionetworks.repo.model.query.jdo.SqlConstants.*;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.sagebionetworks.repo.model.dbo.DatabaseObject;
import org.sagebionetworks.repo.model.dbo.FieldColumn;
import org.sagebionetworks.repo.model.dbo.TableMapping;

public class DBOAccessControlList implements DatabaseObject<DBOAccessControlList> {
	
	private static FieldColumn[] FIELDS = new FieldColumn[] {
		new FieldColumn("id", COL_ID, true),
		new FieldColumn("resource", ACL_OWNER_ID_COLUMN),
		new FieldColumn("createdBy", COL_NODE_CREATED_BY),
		new FieldColumn("creationDate", COL_NODE_CREATED_ON),
		new FieldColumn("modifiedBy", COL_REVISION_MODIFIED_BY),
		new FieldColumn("modifiedOn", COL_REVISION_MODIFIED_ON),
		};

	@Override
	public TableMapping<DBOAccessControlList> getTableMapping() {
		return new TableMapping<DBOAccessControlList>(){

			@Override
			public DBOAccessControlList mapRow(ResultSet rs, int rowNum) throws SQLException {
				DBOAccessControlList acl = new DBOAccessControlList();
				acl.setId(rs.getLong(COL_ID));
				acl.setResource(rs.getLong(ACL_OWNER_ID_COLUMN));
				acl.setCreatedBy(rs.getString(COL_NODE_CREATED_BY));
				acl.setCreationDate(rs.getLong(COL_NODE_CREATED_ON));
				acl.setModifiedBy(rs.getString(COL_REVISION_MODIFIED_BY));
				acl.setModifiedOn(rs.getLong(COL_REVISION_MODIFIED_ON));
				return acl;
			}

			@Override
			public String getTableName() {
				return TABLE_ACCESS_CONTROL_LIST;
			}

			@Override
			public String getDDLFileName() {
				return DDL_FILE_ACL;
			}

			@Override
			public FieldColumn[] getFieldColumns() {
				return FIELDS;
			}

			@Override
			public Class<? extends DBOAccessControlList> getDBOClass() {
				return DBOAccessControlList.class;
			}};
	}
	
	private Long id;
	private Long resource;
	private String createdBy;
	private Long creationDate;
	private String modifiedBy;
	private Long modifiedOn;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getResource() {
		return resource;
	}
	public void setResource(Long resource) {
		this.resource = resource;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public Long getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Long creationDate) {
		this.creationDate = creationDate;
	}
	public String getModifiedBy() {
		return modifiedBy;
	}
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
	public Long getModifiedOn() {
		return modifiedOn;
	}
	public void setModifiedOn(Long modifiedOn) {
		this.modifiedOn = modifiedOn;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((createdBy == null) ? 0 : createdBy.hashCode());
		result = prime * result
				+ ((creationDate == null) ? 0 : creationDate.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((modifiedBy == null) ? 0 : modifiedBy.hashCode());
		result = prime * result
				+ ((modifiedOn == null) ? 0 : modifiedOn.hashCode());
		result = prime * result
				+ ((resource == null) ? 0 : resource.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DBOAccessControlList other = (DBOAccessControlList) obj;
		if (createdBy == null) {
			if (other.createdBy != null)
				return false;
		} else if (!createdBy.equals(other.createdBy))
			return false;
		if (creationDate == null) {
			if (other.creationDate != null)
				return false;
		} else if (!creationDate.equals(other.creationDate))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (modifiedBy == null) {
			if (other.modifiedBy != null)
				return false;
		} else if (!modifiedBy.equals(other.modifiedBy))
			return false;
		if (modifiedOn == null) {
			if (other.modifiedOn != null)
				return false;
		} else if (!modifiedOn.equals(other.modifiedOn))
			return false;
		if (resource == null) {
			if (other.resource != null)
				return false;
		} else if (!resource.equals(other.resource))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "DBOAccessControlList [id=" + id + ", resource=" + resource
				+ ", createdBy=" + createdBy + ", creationDate=" + creationDate
				+ ", modifiedBy=" + modifiedBy + ", modifiedOn=" + modifiedOn
				+ "]";
	}

}
