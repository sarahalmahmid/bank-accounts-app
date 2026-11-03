import { Request, Response } from 'express';
import * as accountsService from '../services/accounts.service';

export async function list(req: Request, res: Response) {
  const accounts = await accountsService.listAccounts(res.locals.customerNumber);
  res.json(accounts);
}