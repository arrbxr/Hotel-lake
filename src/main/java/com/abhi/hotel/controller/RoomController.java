package com.abhi.hotel.controller;

import com.abhi.hotel.exception.PhotoRetrievalException;
import com.abhi.hotel.model.BookedRoom;
import com.abhi.hotel.model.Room;
import com.abhi.hotel.response.BookingResponse;
import com.abhi.hotel.response.RoomResponse;
import com.abhi.hotel.service.IBookedRoomService;
import com.abhi.hotel.service.IRoomService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomController {

    private final IRoomService roomService;
    private final IBookedRoomService bookedRoomService;

    @PostMapping("/add/new-room")
    public ResponseEntity<RoomResponse> addNewRoom(
            @RequestParam("photo") MultipartFile photo,
            @RequestParam("roomType") String roomType,
            @RequestParam("roomPrice") BigDecimal roomPrice) throws SQLException, IOException {
        Room saveRoom = roomService.addNewRoom(photo, roomType, roomPrice);

        RoomResponse response = new RoomResponse(saveRoom.getId(), saveRoom.getRoomType(),
                saveRoom.getRoomPrice());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/room-types")
    public List<String> getRoomTypes() {
        return roomService.getAllRoomTypes();
    }

    @GetMapping("/all")
    public ResponseEntity<List<RoomResponse>> getAllRooms() throws SQLException {
        List<Room> rooms = roomService.getAllRooms();
        List<RoomResponse> roomResponses = new ArrayList<>();

        for (Room room : rooms) {
            byte[] photoBytes = roomService.getRoomPhotoByRoomId(room.getId());

            if (photoBytes != null && photoBytes.length > 0) {
                String base64Photo = Base64.encodeBase64String(photoBytes);
                RoomResponse roomResponse = getRoomResponse(room);
                roomResponse.setPhoto(base64Photo);
                roomResponses.add(roomResponse);
            }
        }

        return new ResponseEntity<>(roomResponses, HttpStatus.OK);
    }

    private RoomResponse getRoomResponse(Room room) {
        List<BookedRoom> bookings = getAllBookingsByRoomId(room.getId());
        List<BookingResponse> bookingResponses = bookings
                .stream().map(bookedRoom -> new BookingResponse(
                        bookedRoom.getBookingId(), bookedRoom.getCheckInDate(), bookedRoom.getCheckOutDate(),
                        bookedRoom.getBookingConfirmationCode())).toList();

        byte[] photoBytes = null;
        Blob photoBlob = room.getPhoto();

        if(photoBlob != null){
            try {
                photoBytes = photoBlob.getBytes(1, (int)photoBlob.length());
            } catch (SQLException e){
                throw new PhotoRetrievalException("Error retrieving photo");
            }
        }

        return new RoomResponse(room.getId(),
                room.getRoomType(),
                room.getRoomPrice(),
                room.isBooked(),
                photoBytes, bookingResponses
        );
    }

    private List<BookedRoom> getAllBookingsByRoomId(Long roomId) {
        return bookedRoomService.getAllBookingsByRoomId(roomId);
    }
}