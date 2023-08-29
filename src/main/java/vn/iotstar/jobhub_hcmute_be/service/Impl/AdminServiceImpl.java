package vn.iotstar.jobhub_hcmute_be.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iotstar.jobhub_hcmute_be.dto.GenericResponse;
import vn.iotstar.jobhub_hcmute_be.entity.Admin;
import vn.iotstar.jobhub_hcmute_be.entity.User;
import vn.iotstar.jobhub_hcmute_be.repository.AdminRepository;
import vn.iotstar.jobhub_hcmute_be.repository.UserRepository;
import vn.iotstar.jobhub_hcmute_be.service.AdminService;

import java.util.List;
import java.util.Optional;

@Service
public class AdminServiceImpl implements AdminService {
   @Autowired
   private AdminRepository adminRepository;

   @Autowired
   private UserRepository userRepository;

    @Override
    @Deprecated
    public Admin getOne(String s) {
        return adminRepository.getOne(s);
    }

    @Override
    public <S extends Admin> List<S> findAll(Example<S> example) {
        return adminRepository.findAll(example);
    }

    @Override
    public List<Admin> findAll() {
        return adminRepository.findAll();
    }

    @Override
    public <S extends Admin> S save(S entity) {
        return adminRepository.save(entity);
    }

    @Override
    public Optional<Admin> findById(String s) {
        return adminRepository.findById(s);
    }

    @Override
    public void deleteById(String s) {
        adminRepository.deleteById(s);
    }


}
