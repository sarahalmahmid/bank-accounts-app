import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import { pool } from '../db';
import { config } from '../config';

export interface LoginResult {
  accessToken: string;
  expiresIn: number;
}

export async function login(customerNumber: number, password: string): Promise<LoginResult | null> {
  const result = await pool.query(
    'SELECT password_hash FROM customers WHERE customer_number = $1',
    [customerNumber]
  );
  const customer = result.rows[0];

  const passwordOk = customer && (await bcrypt.compare(password, customer.password_hash));
  if (!passwordOk) return null;

  const accessToken = jwt.sign({ sub: String(customerNumber) }, config.jwtSecret, {
    expiresIn: config.tokenTtlSeconds,
  });

  return { accessToken, expiresIn: config.tokenTtlSeconds };
}