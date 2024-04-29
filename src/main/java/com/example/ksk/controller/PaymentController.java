package com.example.ksk.controller;

import com.example.ksk.dto.*;
import com.example.ksk.entity.Counter;
import com.example.ksk.entity.Jkh;
import com.example.ksk.entity.Payment;
import com.example.ksk.entity.User;
import com.example.ksk.error.AppError;
import com.example.ksk.repository.*;
import com.example.ksk.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

@RestController
@RequiredArgsConstructor
public class PaymentController {
    private final UserService userService;
    private final PaymentRepository paymentRepository;
    private final HouseRepository houseRepository;
    private final JkhRepository jkhRepository;
    private final RateRepository rateRepository;
    private final CounterRepository counterRepository;

    @PostMapping("/payment/send/{jkhId}/{userId}")
    public ResponseEntity<?> createPayment(@PathVariable(name = "jkhId") Long jkhId,
                                           @PathVariable(name = "userId") Long userId,
                                           @RequestBody PaymentDto paymentDto) {
        Optional<User> userOptional = userService.findById(userId);
        Optional<Jkh> jkhOptional = jkhRepository.findById(jkhId);

        if (userOptional.isPresent() && jkhOptional.isPresent()) {
            User user = userOptional.get();
            Jkh jkh = jkhOptional.get();

            LocalDate date = LocalDate.now();

            Payment payment = new Payment();
            payment.setPrice(paymentDto.getPrice());
            payment.setDeadline(
                    date.getYear() + "-" +
                            (date.getMonthValue() < 10 ? ("0" + date.getMonthValue()) : date.getMonthValue()) + "-" +
                            (date.getDayOfMonth() < 10 ? ("0" + date.getDayOfMonth()) : date.getDayOfMonth()));
            payment.setStatus(0);

            List<CounterDto> counters = paymentDto.getCounters();
            List<Counter> list = counters.stream().map(item -> {
                Counter counter = new Counter();
                counter.setRemovalDate(item.getRemovalDate());
                counter.setMeterReadings(item.getMeterReadings());
                counter.setRateId(item.getRate());

                counterRepository.save(counter);
                return counter;
            }).toList();

            payment.setCounters(list);
            payment.setLodger(user);
            payment.setJkh(jkh);

            user.addPayment(payment);
            userService.saveUser(user);

            return ResponseEntity.ok(new ResponseDto(200, "Payment sent successfully!"));
        }

        return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "User or Jkh not found!"), HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/users/{id}/payments")
    public ResponseEntity<?> getUserPayments(@PathVariable(name = "id") Long id) {
        Optional<User> userOptional = userService.findById(id);

        if (userOptional.isPresent()) {
            List<Payment> payments = userOptional.get().getPayments();

            List<PaymentDto> paymentDtoList = payments.stream().filter(item -> {
                return item.getStatus() == 0 || item.getStatus() == 2;
            }).map(item -> {
                PaymentDto paymentDto = new PaymentDto(item.getId(), item.getPrice(), item.getDeadline(), item.getStatus());
                List<CounterDto> listCounters = item.getCounters().stream().map(counter -> {
                    return new CounterDto(counter.getId(), counter.getMeterReadings(), counter.getRemovalDate(), counter.getRateId());
                }).toList();

                User lodger = item.getLodger();
                Jkh jkh = item.getJkh();

                paymentDto.setCounters(listCounters);
                paymentDto.setUser(
                        new UserDto(lodger.getId(), lodger.getUsername(), lodger.getEmail(), lodger.getFullName(),
                                lodger.isBanned(), lodger.getHouse() != null));
                paymentDto.setJkh(
                        new JkhDto(jkh.getId(), jkh.getLegalAddress(), jkh.getInn(), jkh.getKpp(), jkh.getName(),
                                jkh.getBankAccount(), jkh.getPhoneNumber()));

                return paymentDto;
            }).toList();

            return ResponseEntity.ok(paymentDtoList);
        }

        return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "User not found!"), HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/users/{id}/payments/all")
    public ResponseEntity<?> getUserAllPayments(@PathVariable(name = "id") Long id) {
        Optional<User> userOptional = userService.findById(id);

        if (userOptional.isPresent()) {
            List<Payment> payments = userOptional.get().getPayments();

            List<PaymentDto> paymentDtoList = payments.stream().map(item -> {
                PaymentDto paymentDto = new PaymentDto(item.getId(), item.getPrice(), item.getDeadline(), item.getStatus());
                List<CounterDto> listCounters = item.getCounters().stream().map(counter -> {
                    return new CounterDto(counter.getId(), counter.getMeterReadings(), counter.getRemovalDate(), counter.getRateId());
                }).toList();

                User lodger = item.getLodger();
                Jkh jkh = item.getJkh();

                paymentDto.setCounters(listCounters);
                paymentDto.setUser(
                        new UserDto(lodger.getId(), lodger.getUsername(), lodger.getEmail(), lodger.getFullName(),
                                lodger.isBanned(), lodger.getHouse() != null));
                paymentDto.setJkh(
                        new JkhDto(jkh.getId(), jkh.getLegalAddress(), jkh.getInn(), jkh.getKpp(), jkh.getName(),
                                jkh.getBankAccount(), jkh.getPhoneNumber()));

                return paymentDto;
            }).toList();

            return ResponseEntity.ok(paymentDtoList);
        }

        return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "User not found!"), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/payments/{id}/expired")
    public ResponseEntity<?> expirePayment(@PathVariable(name = "id") Long id) throws ParseException {
        Optional<Payment> paymentOptional = paymentRepository.findById(id);

        if (paymentOptional.isPresent()) {
            Payment payment = paymentOptional.get();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            Date dateFromString = dateFormat.parse(payment.getDeadline());
            Date currentDate = new Date();

            if (dateFromString.before(currentDate)) {
                payment.setStatus(2);

                long diffInMillis = Math.abs(dateFromString.getTime() - currentDate.getTime());
                long diffInDays = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

                float fine = payment.getPrice() * diffInDays;
                fine *= (float) (0.0001 * diffInDays);

                payment.setPrice(payment.getPrice() + fine);
                paymentRepository.save(payment);
                return ResponseEntity.ok(new ResponseDto(200, "Payment is expired!"));
            }
            return ResponseEntity.ok(new ResponseDto(200, "Payment is not expired!"));
        }

        return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Payment not found!"), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/users/{id}/payments/expired")
    public ResponseEntity<?> expirePayments(@PathVariable(name = "id") Long id) throws ParseException {
        Optional<User> userOptional = userService.findById(id);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            System.out.println("Expired " + user);

            List<Payment> payments = user.getPayments();
            System.out.println("Payments count is " + payments.size());

            List<Payment> list = payments.stream().map(payment -> {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                System.out.println("Dates: ");
                Date dateFromString = null;
                try {
                    dateFromString = dateFormat.parse(payment.getDeadline());
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                Date currentDate = new Date();


                if (dateFromString.before(currentDate) && payment.getStatus() != 1) {
                    payment.setStatus(2);

                    long diffInMillis = Math.abs(dateFromString.getTime() - currentDate.getTime());
                    long diffInDays = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

                    float fine = payment.getPrice() * diffInDays;
                    fine *= (float) (0.0001 * diffInDays);

                    payment.setPrice(payment.getPrice() + fine);
                    paymentRepository.save(payment);
                }

                return payment;
            }).toList();

            return ResponseEntity.ok(new ResponseDto(200, "Payment sent successfully!"));
        }

        return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "User not found!"), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/payment/{id}/pay")
    public ResponseEntity<?> payPayments(@PathVariable(name = "id") Long id) {
        Optional<Payment> paymentOptional = paymentRepository.findById(id);

        if (paymentOptional.isPresent()) {
            Payment payment = paymentOptional.get();
            System.out.println(payment);

            payment.setStatus(1);
            System.out.println(payment);
            paymentRepository.save(payment);

            return ResponseEntity.ok(new ResponseDto(200, "Payment payed successfully!"));
        }

        return new ResponseEntity<>(new AppError(HttpStatus.BAD_REQUEST.value(), "Payment not found!"), HttpStatus.BAD_REQUEST);
    }
}
