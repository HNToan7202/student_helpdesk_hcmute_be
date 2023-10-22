package vn.iotstar.jobhub_hcmute_be.service.Impl;

import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.jobhub_hcmute_be.dto.*;
import vn.iotstar.jobhub_hcmute_be.entity.*;
import vn.iotstar.jobhub_hcmute_be.enums.ErrorCodeEnum;
import vn.iotstar.jobhub_hcmute_be.model.ActionResult;
import vn.iotstar.jobhub_hcmute_be.repository.*;
import vn.iotstar.jobhub_hcmute_be.service.CloudinaryService;
import vn.iotstar.jobhub_hcmute_be.service.ResumeService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ResumeServiceImpl implements ResumeService {

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    EducationRepository educationRepository;

    @Autowired
    SocialActivityRepository socialActivityRepository;

    @Autowired
    SocialRepository socialRepository;

    @Autowired
    ExperienceRepository experienceRepository;

    @Autowired
    CertificateRepository certificateRepository;

    @Autowired
    PrizeRepository prizeRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ResumeUploadRepository resumeUploadRepository;

    @Override
    public <S extends Resume> List<S> saveAll(Iterable<S> entities) {
        return resumeRepository.saveAll(entities);
    }

    @Override
    public List<Resume> findAll() {
        return resumeRepository.findAll();
    }

    @Override
    public <S extends Resume> S save(S entity) {
        return resumeRepository.save(entity);
    }

    @Override
    public void deleteById(String s) {
        resumeRepository.deleteById(s);
    }

    @Override
    public void delete(Resume entity) {
        resumeRepository.delete(entity);
    }

    @Override
    public void deleteAll() {
        resumeRepository.deleteAll();
    }

    @Override
    public List<Resume> findAll(Sort sort) {
        return resumeRepository.findAll(sort);
    }

    @Override
    public Page<Resume> findAll(Pageable pageable) {
        return resumeRepository.findAll(pageable);
    }


    @Override
    public ResponseEntity<?> uploadResumeFile(MultipartFile resumeFile, String userId, Boolean isMain) throws IOException {
        String url = cloudinaryService.uploadResume(resumeFile, userId);
        if (url == null) {
            return ResponseEntity.status(500).body(GenericResponse.builder()
                    .success(false)
                    .message("Internal Server Error")
                    .statusCode(500)
                    .build());
        }
        Optional<Student> opt = studentRepository.findById(userId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(GenericResponse.builder()
                    .success(false)
                    .message("User Not Found")
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .build());
        }
        Student student = opt.get();
        if (student.getResume() == null) {
            Resume resume = new Resume();
            //resume.setStudent(student);
            resume.setCreateAt(new Date());
            resume.setUpdateAt(new Date());

            student.setResume(resume);
        }
        ResumeUpload resume = new ResumeUpload();
        resume.setLinkUpload(url);
        resume.setIsMain(isMain);
        //resume.setCandidate(candidate);
        resume.setName(resumeFile.getOriginalFilename());
        resume.setCreateAt(new Date());
        resume.setUpdateAt(new Date());
        resume.setResume(student.getResume());
        resume.setIsActive(true);
        List<ResumeUpload> resumeUploads;
        resumeUploads = student.getResume().getResumeUploads();
        if (resumeUploads == null)
            resumeUploads = new ArrayList<>();
        resumeUploads.add(resume);
        student.getResume().setResumeUploads(resumeUploads);
        student = studentRepository.save(student);
        return ResponseEntity.status(200).body(GenericResponse.builder()
                .success(true)
                .message("Upload Resume Successfully!")
                .result(student.getResume().getResumeUploads().get(student.getResume().getResumeUploads().size() - 1))
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @Override
    public ResponseEntity<?> getResumeUpload(String userId){
        Optional<Student> opt = studentRepository.findById(userId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(GenericResponse.builder()
                    .success(false)
                    .message("User Not Found")
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .build());
        }

        Student student = opt.get();
        if (student.getResume() == null) {
            return ResponseEntity.status(404).body(GenericResponse.builder()
                    .success(false)
                    .message("Resume Not Found")
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .build());
        }
        List<ResumeUpload> resumeUploads = student.getResume().getResumeUploads();
        List<ResumeUpload> list2 = new ArrayList<>();
        for (ResumeUpload resume: resumeUploads
             ) {
            if(resume.getIsActive() == true){
                list2.add(resume);
            }
        }
        //chỉ lấy những resume isActive = true
        return ResponseEntity.status(200).body(GenericResponse.builder()
                .success(true)
                .message("Get Resume Successfully!")
                .result(list2)
                .statusCode(HttpStatus.OK.value())
                .build());
    }

    @Override
    public ResponseEntity<?> updateResume(ResumeDTO resumeDTO, String studentId) {
        Optional<Student> opt = studentRepository.findById(studentId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(GenericResponse.builder()
                    .success(false)
                    .message("Student Not Found")
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .build());
        }
        Student student = opt.get();
        try {
            Resume resume = student.getResume();
            if (resume == null) {
                resume = new Resume();
                resume.setCreateAt(new Date());
                resume.setUpdateAt(new Date());
                resume.setStudent(student);
                student.setResume(resume);
            }
            System.out.println("resumeDTO: " + resumeDTO.getIsEducationsEdited());
            if (resumeDTO.getIsEducationsEdited()) {
                List<Education> educations = new ArrayList<>();
                for (EducationDTO educationDTO : resumeDTO.getEducations()) {
                    Education education = new Education();
                    if (educationDTO.getId() != null && educationDTO.getIsEdit()) {
                        Optional<Education> optEducation = educationRepository.findById(educationDTO.getId());
                        if (optEducation.isEmpty()) {
                            return ResponseEntity.status(404).body(GenericResponse.builder()
                                    .success(false)
                                    .message("Education Not Found")
                                    .statusCode(HttpStatus.NOT_FOUND.value())
                                    .build());
                        }
                        education = optEducation.get();
                        //BeanUtils.copyProperties(educationDTO, education);
                        education.setTitle(educationDTO.getTitle());
                        education.setLocation(educationDTO.getLocation());
                        education.setYearBegin(educationDTO.getYearBegin());
                        education.setYearEnd(educationDTO.getYearEnd());
                        education.setDescription(educationDTO.getDescription());

                        education = educationRepository.save(education);
                        System.out.println("education: " + resume.getEducations().toString());
                    } else if(educationDTO.getId() == null && !educationDTO.getIsEdit()){
                        BeanUtils.copyProperties(educationDTO, education);
                        education.setResume(resume);
                        educations.add(education);
                    }
                    else if(educationDTO.getId()!= null && educationDTO.getIsDelete()){
                        Optional<Education> optionalEducation = educationRepository.findById(educationDTO.getId());
                        if(optionalEducation.isPresent()){
                            educationRepository.deleteById(optionalEducation.get().getId());
                        }
                        else throw new RuntimeException("Education not found");
                    }
                }

                List<Education> educationsOld = resume.getEducations();
                educationsOld.addAll(educations);
                resume.setEducations(educationsOld);

            }
            if (resumeDTO.getIsCoursesEdited()) {
                List<Course> courses = new ArrayList<>();
                for (CourseDTO courseDTO : resumeDTO.getCourses()) {
                    Course course = new Course();
                    if (courseDTO.getId() != null && courseDTO.getIsEdit()) {
                        Optional<Course> optCourse = courseRepository.findById(courseDTO.getId());
                        if (optCourse.isEmpty()) {
                            return ResponseEntity.status(404).body(GenericResponse.builder()
                                    .success(false)
                                    .message("Course Not Found")
                                    .statusCode(HttpStatus.NOT_FOUND.value())
                                    .build());
                        }
                        course = optCourse.get();
                        BeanUtils.copyProperties(courseDTO, course);
                        course = courseRepository.save(course);
                    } else if (courseDTO.getId() == null && !courseDTO.getIsEdit()){
                        BeanUtils.copyProperties(courseDTO, course);
                        course.setResume(resume);
                        courses.add(course);
                    }
                    if(courseDTO.getId()!= null && courseDTO.getIsDelete()){
                        Optional<Education> optionalEducation = educationRepository.findById(courseDTO.getId());
                        if(optionalEducation.isPresent()){
                            educationRepository.deleteById(optionalEducation.get().getId());
                        }
                        else throw new RuntimeException("Education not found");
                    }

                }
                List<Course> coursesOld = resume.getCourses();
                coursesOld.addAll(courses);
                resume.setCourses(coursesOld);
            }

            if (resumeDTO.getIsCertificatesEdited()) {
                List<Certificate> certificates = new ArrayList<>();
                for (CertificateDTO certificateDTO : resumeDTO.getCertificates()) {
                    Certificate certificate = new Certificate();
                    if (certificateDTO.getId() != null && certificateDTO.getIsEdit()) {
                        Optional<Certificate> optCertificate = certificateRepository.findById(certificateDTO.getId());
                        if (optCertificate.isEmpty()) {
                            return ResponseEntity.status(404).body(GenericResponse.builder()
                                    .success(false)
                                    .message("Certificate Not Found")
                                    .statusCode(HttpStatus.NOT_FOUND.value())
                                    .build());
                        }
                        certificate = optCertificate.get();
                        BeanUtils.copyProperties(certificateDTO, certificate);
                        certificate = certificateRepository.save(certificate);
                    } else if (certificateDTO.getId() == null && !certificateDTO.getIsEdit()){
                        BeanUtils.copyProperties(certificateDTO, certificate);
                        certificate.setResume(resume);
                        certificates.add(certificate);
                    }
                     if(certificateDTO.getId()!= null && certificateDTO.getIsDelete()){
                        Optional<Education> optionalEducation = educationRepository.findById(certificate.getId());
                        if(optionalEducation.isPresent()){
                            educationRepository.deleteById(optionalEducation.get().getId());
                        }
                        else throw new RuntimeException("Education not found");
                    }

                }
                List<Certificate> certificatesOld = resume.getCertificates();
                certificatesOld.addAll(certificates);
                resume.setCertificates(certificatesOld);
            }

            if (resumeDTO.getIsExperiencesEdited()) {
                List<Experience> experiences = new ArrayList<>();
                for (ExperienceDTO experienceDTO : resumeDTO.getExperiences()) {
                    Experience experience = new Experience();
                    if (experienceDTO.getId() != null && experienceDTO.getIsEdit()) {
                        Optional<Experience> optExperience = experienceRepository.findById(experienceDTO.getId());
                        if (optExperience.isEmpty()) {
                            return ResponseEntity.status(404).body(GenericResponse.builder()
                                    .success(false)
                                    .message("Experience Not Found")
                                    .statusCode(HttpStatus.NOT_FOUND.value())
                                    .build());
                        }
                        experience = optExperience.get();
                        BeanUtils.copyProperties(experienceDTO, experience);
                        experience = experienceRepository.save(experience);
                    } else if (experienceDTO.getId() == null && !experienceDTO.getIsEdit()){
                        BeanUtils.copyProperties(experienceDTO, experience);
                        experience.setResume(resume);
                        experiences.add(experience);
                    }
                    if(experience.getId()!= null && experienceDTO.getIsDelete()){
                        assert experienceDTO.getId() != null;
                        Optional<Education> optionalEducation = educationRepository.findById(experienceDTO.getId());
                        if(optionalEducation.isPresent()){
                            educationRepository.deleteById(optionalEducation.get().getId());
                        }
                        else throw new RuntimeException("Experience not found");
                    }

                }
                List<Experience> experiencesOld = resume.getExperiences();
                experiencesOld.addAll(experiences);
                resume.setExperiences(experiencesOld);
            }
            if (resumeDTO.getIsPrizesEdited()) {
                List<Prize> prizes = new ArrayList<>();
                for (PrizeDTO prizeDTO : resumeDTO.getPrizes()) {
                    Prize prize = new Prize();
                    if (prizeDTO.getId() != null && prizeDTO.getIsEdit()) {
                        Optional<Prize> optPrize = prizeRepository.findById(prizeDTO.getId());
                        if (optPrize.isEmpty()) {
                            return ResponseEntity.status(404).body(GenericResponse.builder()
                                    .success(false)
                                    .message("Prize Not Found")
                                    .statusCode(HttpStatus.NOT_FOUND.value())
                                    .build());
                        }
                        prize = optPrize.get();
                        BeanUtils.copyProperties(prizeDTO, prize);
                        prize = prizeRepository.save(prize);
                    } else if (prizeDTO.getId() == null && prizeDTO.getIsEdit() == false) {
                        BeanUtils.copyProperties(prizeDTO, prize);
                        prize.setResume(resume);
                        prizes.add(prize);
                    }
                    if(prizeDTO.getId()!= null && prizeDTO.getIsDelete()){
                        Optional<Education> optionalEducation = educationRepository.findById(prizeDTO.getId());
                        if(optionalEducation.isPresent()){
                            educationRepository.deleteById(optionalEducation.get().getId());
                        }
                        else throw new RuntimeException("Prize not found");
                    }

                }
                List<Prize> prizesOld = resume.getPrizes();
                prizesOld.addAll(prizes);
                resume.setPrizes(prizesOld);
            }
            if (resumeDTO.getIsProjectsEdited()) {
                List<Project> projects = new ArrayList<>();
                for (ProjectDTO projectDTO : resumeDTO.getProjects()) {
                    Project project = new Project();
                    if (projectDTO.getId() != null && projectDTO.getIsEdit()) {
                        Optional<Project> optProject = projectRepository.findById(projectDTO.getId());
                        if (optProject.isEmpty()) {
                            return ResponseEntity.status(404).body(GenericResponse.builder()
                                    .success(false)
                                    .message("Project Not Found")
                                    .statusCode(HttpStatus.NOT_FOUND.value())
                                    .build());
                        }
                        project = optProject.get();
                        BeanUtils.copyProperties(projectDTO, project);
                        project = projectRepository.save(project);
                    } else if (projectDTO.getId() == null && !projectDTO.getIsEdit()){
                        BeanUtils.copyProperties(projectDTO, project);
                        project.setResume(resume);
                        projects.add(project);
                    }
                    if(projectDTO.getId()!= null && projectDTO.getIsDelete()){
                        Optional<Education> optionalEducation = educationRepository.findById(projectDTO.getId());
                        if(optionalEducation.isPresent()){
                            educationRepository.deleteById(optionalEducation.get().getId());
                        }
                        else throw new RuntimeException("Project not found");
                    }

                }
                List<Project> projectsOld = resume.getProjects();
                projectsOld.addAll(projects);
                resume.setProjects(projectsOld);
            }

            if (resumeDTO.getIsSocialActivitiesEdited()) {
                List<SocialActivity> socialActivities = new ArrayList<>();
                for (SocialActivityDTO socialActivityDTO : resumeDTO.getSocialActivities()) {
                    SocialActivity socialActivity = new SocialActivity();
                    if (socialActivityDTO.getId() != null && socialActivityDTO.getIsEdit()) {
                        Optional<SocialActivity> optSocialActivity = socialActivityRepository.findById(socialActivityDTO.getId());
                        if (optSocialActivity.isEmpty()) {
                            return ResponseEntity.status(404).body(GenericResponse.builder()
                                    .success(false)
                                    .message("Social Activity Not Found")
                                    .statusCode(HttpStatus.NOT_FOUND.value())
                                    .build());
                        }
                        socialActivity = optSocialActivity.get();
                        BeanUtils.copyProperties(socialActivityDTO, socialActivity);
                        socialActivity = socialActivityRepository.save(socialActivity);
                    } else if (socialActivityDTO.getId() == null && !socialActivityDTO.getIsEdit()){
                        BeanUtils.copyProperties(socialActivityDTO, socialActivity);
                        socialActivity.setResume(resume);
                        socialActivities.add(socialActivity);
                    }
                    if(socialActivityDTO.getId()!= null && socialActivityDTO.getIsDelete()){
                        Optional<Education> optionalEducation = educationRepository.findById(socialActivityDTO.getId());
                        if(optionalEducation.isPresent()){
                            educationRepository.deleteById(optionalEducation.get().getId());
                        }
                        else throw new RuntimeException("Education not found");
                    }

                }
                List<SocialActivity> socialActivitiesOld = resume.getSocialActivities();
                socialActivitiesOld.addAll(socialActivities);
                resume.setSocialActivities(socialActivitiesOld);
            }

            if (resumeDTO.getIsSocialsEdited()) {
                List<Social> socials = new ArrayList<>();
                for (SocialDTO socialDTO : resumeDTO.getSocials()) {
                    Social social = new Social();
                    if (socialDTO.getId() != null && socialDTO.getIsEdit()) {
                        Optional<Social> optSocial = socialRepository.findById(socialDTO.getId());
                        if (optSocial.isEmpty()) {
                            return ResponseEntity.status(404).body(GenericResponse.builder()
                                    .success(false)
                                    .message("Social Not Found")
                                    .statusCode(HttpStatus.NOT_FOUND.value())
                                    .build());
                        }
                        social = optSocial.get();
                        BeanUtils.copyProperties(socialDTO, social);
                        social = socialRepository.save(social);
                    } else if (socialDTO.getId() == null && !socialDTO.getIsEdit()) {
                        BeanUtils.copyProperties(socialDTO, social);
                        social.setResume(resume);
                        socials.add(social);
                    }
                    if(socialDTO.getId()!= null && socialDTO.getIsDelete()){
                        Optional<Education> optionalEducation = educationRepository.findById(socialDTO.getId());
                        if(optionalEducation.isPresent()){
                            educationRepository.deleteById(optionalEducation.get().getId());
                        }
                        else throw new RuntimeException("Education not found");
                    }
                }
                List<Social> socialsOld = resume.getSocials();
                socialsOld.addAll(socials);
                resume.setSocials(socialsOld);
            }

            resume.setUpdateAt(new Date());
            resume.setStudent(student);

            resume = resumeRepository.save(resume);
            return ResponseEntity.status(200).body(GenericResponse.builder()
                    .success(true)
                    .message("Update Resume Successfully!")
                    .result(resume)
                    .statusCode(HttpStatus.OK.value())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(GenericResponse.builder()
                    .success(false)
                    .message("Internal Server Error")
                    .statusCode(500)
                    .build());
            // Xử lý ngoại lệ nếu cần
        }

    }

    @Override
    public ResponseEntity<?> getDetailResume(String studentId) {
        Optional<Student> opt = studentRepository.findById(studentId);

        if (opt.isEmpty()) {
            return ResponseEntity.status(404).body(GenericResponse.builder()
                    .success(false)
                    .message("Student Not Found")
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .build());
        }

        Student student = opt.get();
        return ResponseEntity.status(200).body(GenericResponse.builder()
                .success(true)
                .message("Get Resume Successfully!")
                .result(student.getResume())
                .statusCode(HttpStatus.OK.value())
                .build());
    }


    @Override
    public ActionResult deleteResume(String resumeId, String userId){
        ActionResult actionResult = new ActionResult();
        Optional<ResumeUpload> opt = resumeUploadRepository.findById(resumeId);
        if (opt.isEmpty()) {
            actionResult.setErrorCode(ErrorCodeEnum.CV_NOT_FOUND);
            return actionResult;
        }
        else {
            System.out.println("CV:"+opt.get().getResumeId());
            ResumeUpload resumeUpload = opt.get();
            resumeUpload.setIsActive(false);
            resumeUploadRepository.save(resumeUpload);
            actionResult.setData(resumeUpload);
            actionResult.setErrorCode(ErrorCodeEnum.OK);
            Optional<ResumeUpload> opt2 = resumeUploadRepository.findById(resumeId);
            if (opt2.isPresent()){
                System.out.println("Delete CV failed");
            }
        }
        return actionResult;
    }
}
