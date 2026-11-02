import fs from 'fs';
import path from 'path';
import bcrypt from 'bcryptjs';
import { pool } from './db';

async function seed() {
  const schema = fs.readFileSync(path.join(__dirname, 'schema.sql'), 'utf8');
  await pool.query(schema);

  const passwordHash = bcrypt.hashSync('Password123!', 10);
  await pool.query(
    'INSERT INTO customers (customer_number, full_name, password_hash) VALUES ($1, $2, $3), ($4, $5, $6)',
    [100000001, 'Sarah', passwordHash, 100000002, 'Tareq', passwordHash]
  );

  await pool.query(
    `INSERT INTO accounts (account_number, customer_number, balance, nickname) VALUES
        ($1, $2, $3, $4),
        ($5, $6, $7, $8),
        ($9, $10, $11, $12),
        ($13, $14, $15, $16),
        ($17, $18, $19, $20)`,
    [
      '200000001', 100000001, 2000, 'Salary',
      '200000002', 100000001, 4444.990, 'Fun money',
      '200000003', 100000001, 10000, 'Savings',
      '200000004', 100000002, 10000, null,
      '200000005', 100000002, 10000, 'Savings',
    ]
  );

  console.log('Seeded ✅');
}

seed()
  .catch((err) => {
    console.error('Seed failed ❌', err);
    process.exitCode = 1;
  })
  .finally(() => pool.end());