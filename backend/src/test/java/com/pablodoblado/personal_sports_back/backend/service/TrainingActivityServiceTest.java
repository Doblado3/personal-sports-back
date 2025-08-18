package com.pablodoblado.personal_sports_back.backend.service;

import com.pablodoblado.personal_sports_back.backend.entities.TrainingActivity;
import com.pablodoblado.personal_sports_back.backend.entities.enums.TipoActividad;
import com.pablodoblado.personal_sports_back.backend.mappers.TrainingActivityMapper;
import com.pablodoblado.personal_sports_back.backend.models.TrainingActivityResponseDTO;
import com.pablodoblado.personal_sports_back.backend.repositories.TrainingActivityRepository;
import com.pablodoblado.personal_sports_back.backend.services.impls.TrainingActivityServiceImpl;
import com.pablodoblado.personal_sports_back.backend.specifications.TrainingActivitySpecifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrainingActivityServiceTest {

    @Mock
    private TrainingActivityRepository activityRepository;

    @Mock(name = "trainingActivityMapperImpl")
    private TrainingActivityMapper trainingActivityMapper;

    @InjectMocks
    private TrainingActivityServiceImpl trainingActivityService;

    private TrainingActivity trainingActivity;
    private TrainingActivityResponseDTO trainingActivityResponseDTO;

    @BeforeEach
    void setUp() {
        trainingActivity = new TrainingActivity();
        trainingActivity.setId(1L);
        trainingActivity.setNombre("Test Activity");
        trainingActivity.setTipo(TipoActividad.MOVILIDAD);
        trainingActivity.setFechaComienzo(LocalDateTime.now());

        trainingActivityResponseDTO = TrainingActivityResponseDTO.builder()
                .id(1L)
                .nombre("Test Activity")
                .tipo(TipoActividad.MOVILIDAD)
                .fechaComienzo(OffsetDateTime.of(trainingActivity.getFechaComienzo(), ZoneOffset.UTC))
                .build();
    }

    @Test
    void testListActivities_found() {
        try (MockedStatic<TrainingActivitySpecifications> mockedStatic = mockStatic(TrainingActivitySpecifications.class)) {
            Specification<TrainingActivity> spec = mock(Specification.class);
            mockedStatic.when(() -> TrainingActivitySpecifications.findByDiaTipoZonas(any(), any(), any(), any())).thenReturn(spec);

            when(activityRepository.findAll(spec)).thenReturn(Arrays.asList(trainingActivity));
            when(trainingActivityMapper.mapActivityEntityToResponse(any(TrainingActivity.class))).thenReturn(trainingActivityResponseDTO);

            Optional<List<TrainingActivityResponseDTO>> result = trainingActivityService.listActivities(null, null, null, null);

            assertTrue(result.isPresent());
            assertFalse(result.get().isEmpty());
            assertEquals(1, result.get().size());
            assertEquals(trainingActivityResponseDTO, result.get().get(0));
            verify(activityRepository).findAll(spec);
            verify(trainingActivityMapper).mapActivityEntityToResponse(trainingActivity);
        }
    }

    @Test
    void testListActivities_notFound() {
        try (MockedStatic<TrainingActivitySpecifications> mockedStatic = mockStatic(TrainingActivitySpecifications.class)) {
            Specification<TrainingActivity> spec = mock(Specification.class);
            mockedStatic.when(() -> TrainingActivitySpecifications.findByDiaTipoZonas(any(), any(), any(), any())).thenReturn(spec);

            when(activityRepository.findAll(spec)).thenReturn(Collections.emptyList());

            Optional<List<TrainingActivityResponseDTO>> result = trainingActivityService.listActivities(null, null, null, null);

            assertFalse(result.isPresent());
            verify(activityRepository).findAll(spec);
            verifyNoInteractions(trainingActivityMapper);
        }
    }

    @Test
    void testFindActivityById_found() {
        when(activityRepository.findById(1L)).thenReturn(Optional.of(trainingActivity));
        when(trainingActivityMapper.mapActivityEntityToResponse(any(TrainingActivity.class))).thenReturn(trainingActivityResponseDTO);

        Optional<TrainingActivityResponseDTO> result = trainingActivityService.findActivityById(1L);

        assertTrue(result.isPresent());
        assertEquals(trainingActivityResponseDTO, result.get());
        verify(activityRepository).findById(1L);
        verify(trainingActivityMapper).mapActivityEntityToResponse(trainingActivity);
    }

    @Test
    void testFindActivityById_notFound() {
        when(activityRepository.findById(1L)).thenReturn(Optional.empty());
        when(trainingActivityMapper.mapActivityEntityToResponse(any())).thenReturn(null); 

        Optional<TrainingActivityResponseDTO> result = trainingActivityService.findActivityById(1L);

        assertFalse(result.isPresent());
        verify(activityRepository).findById(1L);
        verify(trainingActivityMapper).mapActivityEntityToResponse(null);
    }

    @Test
    void testDeleteActivityById_exists() {
        when(activityRepository.existsById(1L)).thenReturn(true);

        Boolean result = trainingActivityService.deleteActivityById(1L);

        assertTrue(result);
        verify(activityRepository).existsById(1L);
        verify(activityRepository).deleteById(1L);
    }

    @Test
    void testDeleteActivityById_doesNotExist() {
        when(activityRepository.existsById(1L)).thenReturn(false);

        Boolean result = trainingActivityService.deleteActivityById(1L);

        assertFalse(result);
        verify(activityRepository).existsById(1L);
        verify(activityRepository, never()).deleteById(any());
    }

    @Test
    void testUpdateActivityById_foundAndUpdated() {
        TrainingActivity updatedActivity = new TrainingActivity();
        updatedActivity.setNombre("Updated Activity");
        updatedActivity.setTipo(TipoActividad.FUERZA);

        TrainingActivityResponseDTO updatedResponseDTO = TrainingActivityResponseDTO.builder()
                .id(1L)
                .nombre("Updated Activity")
                .tipo(TipoActividad.FUERZA)
                .fechaComienzo(OffsetDateTime.of(trainingActivity.getFechaComienzo(), ZoneOffset.UTC))
                .build();

        when(activityRepository.findById(1L)).thenReturn(Optional.of(trainingActivity));
        when(activityRepository.save(any(TrainingActivity.class))).thenReturn(trainingActivity); // Mock save to return the modified entity
        when(trainingActivityMapper.mapActivityEntityToResponse(any(TrainingActivity.class))).thenReturn(updatedResponseDTO);

        Optional<TrainingActivityResponseDTO> result = trainingActivityService.updateActivityById(1L, updatedActivity);

        assertTrue(result.isPresent());
        assertEquals(updatedResponseDTO, result.get());
        assertEquals("Updated Activity", trainingActivity.getNombre());
        assertEquals(TipoActividad.FUERZA, trainingActivity.getTipo());
        verify(activityRepository).findById(1L);
        verify(activityRepository).save(trainingActivity); // Verify save is called with the modified entity
        verify(trainingActivityMapper).mapActivityEntityToResponse(trainingActivity); // Verify mapping of the updated entity 
    }

    @Test
    void testUpdateActivityById_notFound() {
        TrainingActivity updatedActivity = new TrainingActivity();
        updatedActivity.setNombre("Updated Activity");

        when(activityRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<TrainingActivityResponseDTO> result = trainingActivityService.updateActivityById(1L, updatedActivity);

        assertFalse(result.isPresent());
        verify(activityRepository).findById(1L);
        verifyNoInteractions(trainingActivityMapper);
    }
}