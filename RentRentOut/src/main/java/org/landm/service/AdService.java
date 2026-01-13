package org.landm.service;

import org.landm.dto.AdDto;
import org.landm.dto.requestDto.CreateAdRequestDto;


public interface AdService {

    public AdDto create(CreateAdRequestDto req, long userId);

   // ItemDto create(CreateItemRequestDto req);
}
