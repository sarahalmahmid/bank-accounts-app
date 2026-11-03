import { Request, Response } from 'express';
import { z } from 'zod';
import * as accountsService from '../services/accounts.service';

const accountNumberSchema = z.string().regex(/^\d{9}$/);

const updateNicknameSchema = z.object({
  nickname: z.string().trim().max(255).nullable(),
});

export async function list(req: Request, res: Response) {
  const accounts = await accountsService.listAccounts(res.locals.customerNumber);
  res.json(accounts);
}

export async function get(req: Request, res: Response) {
  const accountNumber = accountNumberSchema.safeParse(req.params.accountNumber);
  if (!accountNumber.success) {
    return res.status(400).json({ error: 'Invalid account number' });
  }

  const account = await accountsService.getAccount(res.locals.customerNumber, accountNumber.data);
  if (!account) {
    return res.status(404).json({ error: 'Account not found' });
  }

  res.json(account);
}

export async function updateNickname(req: Request, res: Response) {
  const accountNumber = accountNumberSchema.safeParse(req.params.accountNumber);
  if (!accountNumber.success) {
    return res.status(400).json({ error: 'Invalid account number' });
  }

  const body = updateNicknameSchema.safeParse(req.body);
  if (!body.success) {
    return res.status(400).json({ error: 'Nickname must be 255 characters or fewer' });
  }

  const nickname = body.data.nickname || null;

  const account = await accountsService.updateNickname(
    res.locals.customerNumber,
    accountNumber.data,
    nickname
  );
  if (!account) {
    return res.status(404).json({ error: 'Account not found' });
  }

  res.json(account);
}