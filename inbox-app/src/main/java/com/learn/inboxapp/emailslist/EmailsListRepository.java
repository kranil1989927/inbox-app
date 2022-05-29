package com.learn.inboxapp.emailslist;

import java.util.List;

import org.springframework.data.cassandra.repository.AllowFiltering;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailsListRepository extends CassandraRepository<EmailsList, EmailsListPrimaryKey> {

	@AllowFiltering
	List<EmailsList> findAllById_UserIdAndId_Label(String userId, String label); 
}
