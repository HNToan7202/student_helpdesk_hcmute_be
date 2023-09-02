package vn.iotstar.jobhub_hcmute_be.dto;

import vn.iotstar.jobhub_hcmute_be.constant.JobType;
import vn.iotstar.jobhub_hcmute_be.entity.Position;
import vn.iotstar.jobhub_hcmute_be.entity.Skill;

import java.util.Date;
import java.util.List;

public class JobDTO {
    private String jobId;
    private String name;
    private JobType jobType;
    private Integer quantity;
    private String benefit;
    private String salaryRange;
    private String requirement;
    private String location;
    private String description;
    private Boolean isActive;
    private Date createdAt;
    private Date updatedAt;
    private Date deadline;
    private Position position;
    private List<Skill> skills;
}
