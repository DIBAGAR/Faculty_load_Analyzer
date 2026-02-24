package com.example.facultyload.service;

import com.example.facultyload.dto.timetable.TimetableCreateRequest;
import com.example.facultyload.dto.timetable.TimetableResponse;

public interface TimetableService {
    TimetableResponse createDraft(TimetableCreateRequest request, String createdByEmail);
    TimetableResponse activate(Long timetableId);
    TimetableResponse getActive(Long departmentId, Integer yearOfStudy, String section);
}

