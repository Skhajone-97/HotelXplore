Feature: Create Booking

  Scenario: Create a new walk-in booking for a new guest with cash payment
    Given I am logged in
    When I navigate to bookings page
    And I create a new booking with guest details
    Then the booking should be created successfully

  Scenario: Create a reservation booking for 5 rooms and 6 nights with cash payment
    Given I am logged in
    When I navigate to bookings page
    And I create a reservation booking for 5 rooms and 6 nights
    Then the reservation booking should be created successfully
