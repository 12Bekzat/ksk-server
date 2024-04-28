package com.example.ksk.dto;

import com.example.ksk.entity.House;
import com.example.ksk.entity.Jkh;
import com.example.ksk.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HouseDto {
    private Long id;
    private String address;
    private float square;
    private int countOfPeople;
    private Long jkhId;

    private UserDto owner;

    public HouseDto(House house) {
        this.id = house.getId();
        this.address = house.getAddress();
        this.square = house.getSquare();
        this.countOfPeople = house.getCountOfPeople();
        this.jkhId = house.getJkh().getId();

        User owner1 = house.getOwner();
        this.owner = new UserDto(owner1.getId(),
                owner1.getUsername(),
                owner1.getEmail(),
                owner1.getFullName(),
                owner1.isBanned(),
                owner1.getHouse() != null);
    }
}
