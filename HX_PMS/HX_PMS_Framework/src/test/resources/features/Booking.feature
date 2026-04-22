Feature: Create Booking

  @walkin
  Scenario: Create a new walk-in booking for a new guest with cash payment
    Given I am logged in
    When I navigate to bookings page
    And I create a new booking with guest details
    Then the booking should be created successfully

  @reservation
  Scenario: Create a reservation booking for 5 rooms and 6 nights with cash payment
    Given I am logged in
    When I navigate to bookings page
    And I create a reservation booking for 5 rooms and 6 nights
    Then the reservation booking should be created successfully

  @walkin-nsdb
  Scenario: Create a walk-in booking for Non Smoking Double Beds room type with cash payment
    Given I am logged in
    When I navigate to bookings page
    And I create a walk-in booking for NSDB room type
    Then the booking should be created successfully

  @reservation-nsdb
  Scenario: Create a reservation booking for Non Smoking Double Beds room type with cash payment
    Given I am logged in
    When I navigate to bookings page
    And I create a reservation booking for NSDB room type
    Then the reservation booking should be created successfully
