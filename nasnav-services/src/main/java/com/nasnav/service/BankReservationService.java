package com.nasnav.service;

import com.nasnav.dto.response.BankReservationDTO;

import java.util.List;

public interface BankReservationService {
    //create new reserve
    public BankReservationDTO createReservation(long accountId, long amount);
    public List<BankReservationDTO> getReservations(Boolean isFulfilled);
    public BankReservationDTO getReservationById(long reservationId);
    //fulfil record
    public void fulfilReservation(long reservationId);

}
