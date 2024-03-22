package com.abhi.hotel.service;

import com.abhi.hotel.model.BookedRoom;
import org.springframework.stereotype.Service;

import java.util.List;

public interface IBookedRoomService {
    List<BookedRoom> getAllBookingsByRoomId(Long roomId);
}
