package com.impacta.login.repository;
import org.springframework.data.repository.CrudRepository;

import com.impacta.login.model.LoginDao;
public interface UserRepository extends CrudRepository<LoginDao, Integer> {

	LoginDao findByUsername(String user_login_cpf);
 
}