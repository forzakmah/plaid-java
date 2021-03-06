package com.plaid.client.integration;

import com.plaid.client.request.AccountsGetRequest;
import com.plaid.client.request.common.Product;
import com.plaid.client.response.Account;
import com.plaid.client.response.AccountsGetResponse;
import com.plaid.client.response.ErrorResponse;
import org.junit.Ignore;
import org.junit.Test;
import retrofit2.Response;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class AccountsGetTest extends AbstractItemIntegrationTest {
  @Override
  protected List<Product> setupItemProducts() {
    return Collections.singletonList(Product.TRANSACTIONS);
  }

  @Override
  protected String setupItemInstitutionId() {
    return TARTAN_BANK_INSTITUTION_ID;
  }

  @Test
  public void testAccountsGetSuccess() throws Exception {
    Response<AccountsGetResponse> response = client().service().accountsGet(
      new AccountsGetRequest(getItemPublicTokenExchangeResponse().getAccessToken()))
      .execute();

    assertSuccessResponse(response);

    // item should be the same one we created
    assertItemEquals(getItem(), response.body().getItem());

    // sandbox should return expected accounts
    List<Account> accounts = response.body().getAccounts();
    assertEquals(8, accounts.size());
    assertAccount(accounts.get(0), "depository", "checking", 100d,
      110d, null, "Plaid Checking",
      "0000", "Plaid Gold Standard 0% Interest Checking");
    assertAccount(accounts.get(1), "depository",
      "savings", 200d, 210d, null, "Plaid Saving",
      "1111", "Plaid Silver Standard 0.1% Interest Saving");
    assertAccount(accounts.get(2), "depository",
      "cd", null, 1000d, null, "Plaid CD",
      "2222", "Plaid Bronze Standard 0.2% Interest CD");
    assertAccount(accounts.get(3), "credit", "credit card", null, 410d, 2000d, "Plaid Credit Card",
      "3333", "Plaid Diamond 12.5% APR Interest Credit Card");
  }

  @Test
  public void testAccountGetWithAccountId() throws Exception {
    // first call to get an account ID
    Response<AccountsGetResponse> response = client().service().accountsGet(
      new AccountsGetRequest(getItemPublicTokenExchangeResponse().getAccessToken()))
      .execute();
    assertSuccessResponse(response);
    String accountId = response.body().getAccounts().get(1).getAccountId();

    // call under test
    response = client().service().accountsGet(
      new AccountsGetRequest(getItemPublicTokenExchangeResponse().getAccessToken()).withAccountIds(Arrays.asList(accountId)))
      .execute();
    assertSuccessResponse(response);

    // item should be the same one we created
    assertItemEquals(getItem(), response.body().getItem());

    // sandbox should return expected accounts
    List<Account> accounts = response.body().getAccounts();
    assertEquals(1, accounts.size());
    assertAccount(accounts.get(0), "depository",
      "savings", 200d, 210d, null, "Plaid Saving",
      "1111", "Plaid Silver Standard 0.1% Interest Saving");
  }

  @Test
  @Ignore("This test fails because the request triggers a sandbox server 500.")
  public void testAccountGetInvalidAccountId() throws Exception {
    Response<AccountsGetResponse> response = client().service().accountsGet(
      new AccountsGetRequest(getItemPublicTokenExchangeResponse().getAccessToken()).withAccountIds(Arrays.asList("not-real")))
      .execute();
    assertErrorResponse(response, ErrorResponse.ErrorType.INVALID_INPUT, "INVALID_ACCOUNT_ID");
  }

  @Test
  public void testAccountGetInvalidAccessToken() throws Exception {
    Response<AccountsGetResponse> response = client().service().accountsGet(
      new AccountsGetRequest("notreal"))
      .execute();
    assertErrorResponse(response, ErrorResponse.ErrorType.INVALID_INPUT, "INVALID_ACCESS_TOKEN");
  }

}
