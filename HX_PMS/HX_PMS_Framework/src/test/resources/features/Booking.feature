Feature: Create Booking

  Scenario: Create a new walk-in booking for an existing guest with cash payment
    Given I am logged in
    When I navigate to bookings page
    And I create a new booking with guest details
    Then the booking should be created successfully