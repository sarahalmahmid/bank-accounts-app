import { pool } from '../db';

export interface Account {
  accountNumber: string;
  balance: number;
  nickname: string | null;
}

interface AccountRow {
  account_number: string;
  balance: string;
  nickname: string | null;
}

function toAccount(row: AccountRow): Account {
  return {
    accountNumber: row.account_number,
    balance: Number(row.balance),
    nickname: row.nickname,
  };
}

export async function listAccounts(customerNumber: number): Promise<Account[]> {
  const result = await pool.query<AccountRow>(
    `SELECT account_number, balance, nickname
       FROM accounts
      WHERE customer_number = $1
      ORDER BY account_number`,
    [customerNumber]
  );
  return result.rows.map(toAccount);
}

export async function getAccount(
  customerNumber: number,
  accountNumber: string
): Promise<Account | null> {
  const result = await pool.query<AccountRow>(
    `SELECT account_number, balance, nickname
       FROM accounts
      WHERE account_number = $1 AND customer_number = $2`,
    [accountNumber, customerNumber]
  );
  return result.rows[0] ? toAccount(result.rows[0]) : null;
}

export async function updateNickname(
  customerNumber: number,
  accountNumber: string,
  nickname: string | null
): Promise<Account | null> {
  const result = await pool.query<AccountRow>(
    `UPDATE accounts
        SET nickname = $1
      WHERE account_number = $2 AND customer_number = $3
      RETURNING account_number, balance, nickname`,
    [nickname, accountNumber, customerNumber]
  );
  return result.rows[0] ? toAccount(result.rows[0]) : null;
}