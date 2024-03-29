package bsep.mojakuca.service;

import bsep.mojakuca.model.Role;
import bsep.mojakuca.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

  @Autowired
  private RoleRepository roleRepository;

  public Role findById(Long id) {
    Role auth = this.roleRepository.getOne(id);
    return auth;
  }

  public List<Role> findByName(String name) {
	List<Role> roles = this.roleRepository.findByName(name);
    return roles;
  }


}
