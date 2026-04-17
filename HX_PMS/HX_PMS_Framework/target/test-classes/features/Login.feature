Feature: Login to HX PMS

  Scenario Outline: Successful login with valid credentials
    Given I am on the login page
    When I enter username "<username>" and password "<password>"
    And I click the login button
    Then I should be logged in successfully

    Examples:
      | username | password |
      | webdev   | 1234     |