package com.pablodoblado.personal_sports_back.backend.service;

import com.pablodoblado.personal_sports_back.backend.entities.TrainingActivity;
import com.pablodoblado.personal_sports_back.backend.entities.Usuario;
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
import java.util.UUID;

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
    private UUID testUserId;
    private List<TrainingActivity> listOfActivities;
    private LocalDateTime randomDate;

    @BeforeEach
    void setUp() {
    	
    	randomDate = LocalDateTime.now().plusDays(4);
    	
    	testUserId = UUID.randomUUID();
        Usuario testUser = new Usuario();
        testUser.setId(testUserId);
    	
        trainingActivity = new TrainingActivity();
        trainingActivity.setId(1L);
        trainingActivity.setUsuario(testUser);
        trainingActivity.setNombre("Test Activity");
        trainingActivity.setTipo(TipoActividad.MOVILIDAD);
        trainingActivity.setFechaComienzo(LocalDateTime.now());
        
        listOfActivities = List.of(trainingActivity);

        trainingActivityResponseDTO = TrainingActivityResponseDTO.builder()
                .id(1L)
                .nombre("Test Activity")
                .tipo(TipoActividad.MOVILIDAD)
                .fechaComienzo(OffsetDateTime.of(trainingActivity.getFechaComienzo(), ZoneOffset.UTC))
                .build();
    }
    
    @Test
    void testFindActivitiesByUsuarioAndDateRange() {
    	
    	when(activityRepository.findAllByUsuario_IdAndFechaComienzoBetween(testUserId, trainingActivity.getFechaComienzo().minusDays(2),
    			trainingActivity.getFechaComienzo().plusDays(2))).thenReturn(listOfActivities);
    	
    	when(trainingActivityMapper.mapActivityEntityToResponse(trainingActivity)).thenReturn(trainingActivityResponseDTO);
    	
    	Optional<List<TrainingActivityResponseDTO>> result = trainingActivityService.findActivitiesByUsuarioDateRange(testUserId, trainingActivity.getFechaComienzo().minusDays(2),
    			trainingActivity.getFechaComienzo().plusDays(2));
    	
    	assertTrue(result.isPresent());
    	assertTrue(result.get().size() == 1);
    }
    
    @Test
    void testFindActivitiesByUsuarioAndDateRangeNotFound() {
    	
    	when(activityRepository.findAllByUsuario_IdAndFechaComienzoBetween(testUserId, randomDate,
    			randomDate.plusDays(10))).thenReturn(Collections.emptyList());
    	
    	Optional<List<TrainingActivityResponseDTO>> result = trainingActivityService.findActivitiesByUsuarioDateRange(testUserId, randomDate,
    			randomDate.plusDays(10));
    	
    	assertFalse(result.isPresent());
    	
    	
    }
    
    @Test
    void testFindActivitiesByUsuarioAndDate() {
    	
    	when(activityRepository.findByUsuario_IdAndFechaComienzo(testUserId, trainingActivity.getFechaComienzo())).thenReturn(listOfActivities);
    	when(trainingActivityMapper.mapActivityEntityToResponse(trainingActivity)).thenReturn(trainingActivityResponseDTO);
    	
    	Optional<List<TrainingActivityResponseDTO>> result = trainingActivityService.findActivitiesByUsuarioAndDate(testUserId, trainingActivity.getFechaComienzo());
    	
    	assertTrue(result.isPresent());
    	assertTrue(result.get().size() == 1);
    	
    }
    
    @Test
    void testFindActivitiesByUsuarioAndDateNotFound() {
    	
    	// Cuando no hay actividades que devolver no se hace mapeo
    	when(activityRepository.findByUsuario_IdAndFechaComienzo(testUserId, randomDate)).thenReturn(Collections.emptyList());
    	
    	
    	Optional<List<TrainingActivityResponseDTO>> resultado = trainingActivityService.findActivitiesByUsuarioAndDate(testUserId, randomDate);
    	
    	assertFalse(resultado.isPresent());
    }

    @Test
    void testListActivitiesFound() {
    	
        try (MockedStatic<TrainingActivitySpecifications> mockedStatic = mockStatic(TrainingActivitySpecifications.class)) {
            Specification<TrainingActivity> spec = mock(Specification.class);
            mockedStatic.when(() -> TrainingActivitySpecifications.findByDiaTipoZonas(any(), any(), any(), any())).thenReturn(spec);

            when(activityRepository.findAll(spec)).thenReturn(Arrays.asList(trainingActivity));
            when(trainingActivityMapper.mapActivityEntityToResponse(any(TrainingActivity.class))).thenReturn(trainingActivityResponseDTO);

            Optional<List<TrainingActivityResponseDTO>> result = trainingActivityService.listActivities(null, null, null, null);

            assertTrue(result.isPresent());
            assertEquals(1, result.get().size());
            assertEquals(trainingActivityResponseDTO, result.get().get(0));
            
            verify(activityRepository).findAll(spec);
            verify(trainingActivityMapper).mapActivityEntityToResponse(trainingActivity);
        }
    }

    @Test
    void testListActivitiesNotFound() {
    	
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
    void testFindActivityByIdFound() {
    	
        when(activityRepository.findById(1L)).thenReturn(Optional.of(trainingActivity));
        when(trainingActivityMapper.mapActivityEntityToResponse(any(TrainingActivity.class))).thenReturn(trainingActivityResponseDTO);

        Optional<TrainingActivityResponseDTO> result = trainingActivityService.findActivityById(1L);

        assertTrue(result.isPresent());
        assertEquals(trainingActivityResponseDTO, result.get());
        
        verify(trainingActivityMapper).mapActivityEntityToResponse(trainingActivity);
    }

    @Test
    void testFindActivityByIdNotFound() {
    	
        when(activityRepository.findById(1L)).thenReturn(Optional.empty());
        when(trainingActivityMapper.mapActivityEntityToResponse(any())).thenReturn(null); 

        Optional<TrainingActivityResponseDTO> result = trainingActivityService.findActivityById(1L);

        assertFalse(result.isPresent());
        
        verify(trainingActivityMapper).mapActivityEntityToResponse(null);
    }

    @Test
    void testDeleteActivityByIdExists() {
    	
        when(activityRepository.existsById(1L)).thenReturn(true);

        Boolean result = trainingActivityService.deleteActivityById(1L);

        assertTrue(result);
        verify(activityRepository).existsById(1L);
        verify(activityRepository).deleteById(1L);
    }

    @Test
    void testDeleteActivityByIdNotExist() {
    	
        when(activityRepository.existsById(1L)).thenReturn(false);

        Boolean result = trainingActivityService.deleteActivityById(1L);

        assertFalse(result);
        verify(activityRepository).existsById(1L);
        verify(activityRepository, never()).deleteById(any());
    }

    @Test
    void testUpdateActivityById() {
    	
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
        when(activityRepository.save(any(TrainingActivity.class))).thenReturn(trainingActivity); 
        when(trainingActivityMapper.mapActivityEntityToResponse(any(TrainingActivity.class))).thenReturn(updatedResponseDTO);

        Optional<TrainingActivityResponseDTO> result = trainingActivityService.updateActivityById(1L, updatedActivity);

        assertTrue(result.isPresent());
        assertEquals(updatedResponseDTO, result.get());
        
        
        verify(activityRepository).findById(1L);
        verify(activityRepository).save(trainingActivity); 
        verify(trainingActivityMapper).mapActivityEntityToResponse(trainingActivity); 
    }

    @Test
    void testUpdateActivityByIdNotFound() {
    	
        TrainingActivity updatedActivity = new TrainingActivity();
        updatedActivity.setNombre("Updated Activity");

        when(activityRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<TrainingActivityResponseDTO> result = trainingActivityService.updateActivityById(1L, updatedActivity);

        assertFalse(result.isPresent());
        verify(activityRepository).findById(1L);
        
    }
}