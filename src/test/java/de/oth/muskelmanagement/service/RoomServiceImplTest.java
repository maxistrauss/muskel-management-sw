package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.dto.RoomDto;
import de.oth.muskelmanagement.model.entity.Equipment;
import de.oth.muskelmanagement.model.entity.Room;
import de.oth.muskelmanagement.model.enums.EquipmentCategory;
import de.oth.muskelmanagement.model.enums.EquipmentStatus;
import de.oth.muskelmanagement.repository.EquipmentRepository;
import de.oth.muskelmanagement.repository.RoomRepository;
import de.oth.muskelmanagement.service.impl.RoomServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceImplTest {

    @Mock
    private RoomRepository roomRepository;
    @Mock
    private EquipmentRepository equipmentRepository;

    @InjectMocks
    private RoomServiceImpl roomService;

    private RoomDto roomDto;
    private Room room;

    @BeforeEach
    void setUp() {
        roomDto = new RoomDto();
        roomDto.setId(1L);
        roomDto.setName("Weight Training Room");
        roomDto.setCapacity(20);
        roomDto.setAmenities("Mirrors, Air conditioning, Sound system");
        roomDto.setActive(true);

        room = new Room("Weight Training Room", 20, "Mirrors, Air conditioning, Sound system", true);
        room.setId(1L);
    }

    @Test
    void save_shouldCreateRoomWithAllProperties() {
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Room savedRoom = roomService.save(roomDto);

        assertNotNull(savedRoom);
        assertEquals("Weight Training Room", savedRoom.getName());
        assertEquals(20, savedRoom.getCapacity());
        assertEquals("Mirrors, Air conditioning, Sound system", savedRoom.getAmenities());
        assertTrue(savedRoom.isActive());
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    void save_shouldCreateInactiveRoom() {
        roomDto.setActive(false);

        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Room savedRoom = roomService.save(roomDto);

        assertNotNull(savedRoom);
        assertFalse(savedRoom.isActive());
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    void save_shouldCreateRoomWithNullAmenities() {
        roomDto.setAmenities(null);

        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Room savedRoom = roomService.save(roomDto);

        assertNotNull(savedRoom);
        assertNull(savedRoom.getAmenities());
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    void findById_shouldReturnRoomDtoIfExists() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(equipmentRepository.findByRoomId(1L)).thenReturn(new ArrayList<>());

        RoomDto foundRoom = roomService.findById(1L);

        assertNotNull(foundRoom);
        assertEquals(1L, foundRoom.getId());
        assertEquals("Weight Training Room", foundRoom.getName());
        assertEquals(20, foundRoom.getCapacity());
        assertEquals("Mirrors, Air conditioning, Sound system", foundRoom.getAmenities());
        assertTrue(foundRoom.isActive());
        verify(roomRepository, times(1)).findById(1L);
    }

    @Test
    void findById_shouldThrowExceptionIfRoomDoesNotExist() {
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> roomService.findById(1L));

        assertEquals("Room not found with id: 1", exception.getMessage());
        verify(roomRepository, times(1)).findById(1L);
    }

    @Test
    void findById_shouldIncludeEquipmentInDto() {
        Equipment equipment1 = new Equipment();
        equipment1.setId(1L);
        equipment1.setName("Treadmill");
        equipment1.setSerialNumber("TM-001");
        equipment1.setStatus(EquipmentStatus.AVAILABLE);
        equipment1.setRoom(room);
        equipment1.setCategory(EquipmentCategory.CARDIO);
        equipment1.setPurchaseDate(LocalDate.now());

        Equipment equipment2 = new Equipment();
        equipment2.setId(2L);
        equipment2.setName("Bike");
        equipment2.setSerialNumber("BK-001");
        equipment2.setStatus(EquipmentStatus.AVAILABLE);
        equipment2.setRoom(room);
        equipment2.setCategory(EquipmentCategory.CARDIO);
        equipment2.setPurchaseDate(LocalDate.now());

        List<Equipment> equipmentList = Arrays.asList(equipment1, equipment2);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(equipmentRepository.findByRoomId(1L)).thenReturn(equipmentList);

        RoomDto foundRoom = roomService.findById(1L);

        assertNotNull(foundRoom);
        assertEquals(2, foundRoom.getEquipmentCount());
        assertNotNull(foundRoom.getEquipment());
        assertEquals(2, foundRoom.getEquipment().size());
        verify(equipmentRepository, times(1)).findByRoomId(1L);
    }

    @Test
    void update_shouldUpdateRoomDetails() {
        Room existingRoom = new Room("Old Room", 10, "Old amenities", true);
        existingRoom.setId(1L);

        RoomDto updateDto = new RoomDto();
        updateDto.setId(1L);
        updateDto.setName("Updated Room");
        updateDto.setCapacity(30);
        updateDto.setAmenities("New amenities");
        updateDto.setActive(false);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(existingRoom));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        roomService.update(updateDto);

        assertEquals("Updated Room", existingRoom.getName());
        assertEquals(30, existingRoom.getCapacity());
        assertEquals("New amenities", existingRoom.getAmenities());
        assertFalse(existingRoom.isActive());
        verify(roomRepository, times(1)).save(existingRoom);
    }

    @Test
    void update_shouldThrowExceptionIfRoomNotFound() {
        RoomDto updateDto = new RoomDto();
        updateDto.setId(99L);
        updateDto.setName("Updated Room");
        updateDto.setCapacity(30);
        updateDto.setAmenities("New amenities");
        updateDto.setActive(true);

        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> roomService.update(updateDto));

        assertEquals("Room not found with id: 99", exception.getMessage());
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void deactivate_shouldSetActiveToFalse() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(equipmentRepository.findByRoomId(1L)).thenReturn(new ArrayList<>());
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        roomService.deactivate(1L);

        assertFalse(room.isActive());
        verify(roomRepository, times(1)).save(room);
        verify(equipmentRepository, times(1)).findByRoomId(1L);
    }

    @Test
    void deactivate_shouldUnassignEquipmentBeforeDeactivating() {
        Equipment equipment1 = new Equipment();
        equipment1.setId(1L);
        equipment1.setName("Treadmill");
        equipment1.setSerialNumber("TM-001");
        equipment1.setStatus(EquipmentStatus.AVAILABLE);
        equipment1.setRoom(room);

        Equipment equipment2 = new Equipment();
        equipment2.setId(2L);
        equipment2.setName("Bike");
        equipment2.setSerialNumber("BK-001");
        equipment2.setStatus(EquipmentStatus.AVAILABLE);
        equipment2.setRoom(room);

        List<Equipment> equipmentList = Arrays.asList(equipment1, equipment2);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(equipmentRepository.findByRoomId(1L)).thenReturn(equipmentList);
        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        roomService.deactivate(1L);

        assertNull(equipment1.getRoom());
        assertNull(equipment2.getRoom());
        assertFalse(room.isActive());
        verify(equipmentRepository, times(2)).save(any(Equipment.class));
        verify(roomRepository, times(1)).save(room);
    }

    @Test
    void deactivate_shouldThrowExceptionIfRoomNotFound() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> roomService.deactivate(99L));

        assertEquals("Room not found with id: 99", exception.getMessage());
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void delete_shouldDeleteRoom() {
        when(equipmentRepository.findByRoomId(1L)).thenReturn(new ArrayList<>());
        doNothing().when(roomRepository).deleteById(1L);

        roomService.delete(1L);

        verify(equipmentRepository, times(1)).findByRoomId(1L);
        verify(roomRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_shouldUnassignEquipmentBeforeDeleting() {
        Equipment equipment1 = new Equipment();
        equipment1.setId(1L);
        equipment1.setName("Treadmill");
        equipment1.setSerialNumber("TM-001");
        equipment1.setStatus(EquipmentStatus.AVAILABLE);
        equipment1.setRoom(room);

        List<Equipment> equipmentList = Arrays.asList(equipment1);

        when(equipmentRepository.findByRoomId(1L)).thenReturn(equipmentList);
        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(roomRepository).deleteById(1L);

        roomService.delete(1L);

        assertNull(equipment1.getRoom());
        verify(equipmentRepository, times(1)).save(equipment1);
        verify(roomRepository, times(1)).deleteById(1L);
    }

    @Test
    void findActiveRooms_shouldReturnOnlyActiveRooms() {
        Room activeRoom1 = new Room("Active Room 1", 20, "Amenities", true);
        activeRoom1.setId(1L);
        Room activeRoom2 = new Room("Active Room 2", 15, "Amenities", true);
        activeRoom2.setId(2L);

        List<Room> activeRooms = Arrays.asList(activeRoom1, activeRoom2);

        when(roomRepository.findByActiveTrue()).thenReturn(activeRooms);

        List<RoomDto> result = roomService.findActiveRooms();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(RoomDto::isActive));
        verify(roomRepository, times(1)).findByActiveTrue();
    }

    @Test
    void findActiveRooms_shouldReturnEmptyListIfNoActiveRooms() {
        when(roomRepository.findByActiveTrue()).thenReturn(new ArrayList<>());

        List<RoomDto> result = roomService.findActiveRooms();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(roomRepository, times(1)).findByActiveTrue();
    }

    @Test
    void unassignEquipmentFromRoom_shouldUnassignAllEquipment() {
        Equipment equipment1 = new Equipment();
        equipment1.setId(1L);
        equipment1.setName("Treadmill");
        equipment1.setSerialNumber("TM-001");
        equipment1.setStatus(EquipmentStatus.AVAILABLE);
        equipment1.setRoom(room);

        Equipment equipment2 = new Equipment();
        equipment2.setId(2L);
        equipment2.setName("Bike");
        equipment2.setSerialNumber("BK-001");
        equipment2.setStatus(EquipmentStatus.AVAILABLE);
        equipment2.setRoom(room);

        Equipment equipment3 = new Equipment();
        equipment3.setId(3L);
        equipment3.setName("Bench");
        equipment3.setSerialNumber("BN-001");
        equipment3.setStatus(EquipmentStatus.AVAILABLE);
        equipment3.setRoom(room);

        List<Equipment> equipmentList = Arrays.asList(equipment1, equipment2, equipment3);

        when(equipmentRepository.findByRoomId(1L)).thenReturn(equipmentList);
        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        roomService.unassignEquipmentFromRoom(1L);

        assertNull(equipment1.getRoom());
        assertNull(equipment2.getRoom());
        assertNull(equipment3.getRoom());
        verify(equipmentRepository, times(3)).save(any(Equipment.class));
    }

    @Test
    void unassignEquipmentFromRoom_shouldHandleEmptyEquipmentList() {
        when(equipmentRepository.findByRoomId(1L)).thenReturn(new ArrayList<>());

        roomService.unassignEquipmentFromRoom(1L);

        verify(equipmentRepository, times(1)).findByRoomId(1L);
        verify(equipmentRepository, never()).save(any(Equipment.class));
    }
}

