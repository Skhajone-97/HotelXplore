Feature: Check In Guest

  @checkin
  Scenario: Check in a guest
    Given I am logged in
    When I navigate to arrivals page
    And I select a booking and check in
    Then the guest should be checked in successfully