import { pool } from '../db';

export interface Account {
  accountNumber: string;
  balance: number;
  nickname: string | null;
}

export async function listAccounts(customerNumber: number): Promise<Account[]> {
  const result = await pool.query(
    `SELECT account_number, balance, nickname
       FROM accounts
      WHERE customer_number = $1
      ORDER BY account_number`,
    [customerNumber]
  );

  return result.rows.map((row) => ({
    accountNumber: row.account_number,
    balance: Number(row.balance),
    nickname: row.nickname,
  }));
}