import { Request, Response } from 'express';
import { z } from 'zod';
import * as authService from '../services/auth.service';

const loginSchema = z.object({
  customerNumber: z.number().int().min(100000000).max(999999999),
  password: z.string().min(1),
});

export async function login(req: Request, res: Response) {
  const parsed = loginSchema.safeParse(req.body);
  if (!parsed.success) {
    return res.status(400).json({ error: 'Invalid request' });
  }

  const result = await authService.login(parsed.data.customerNumber, parsed.data.password);
  if (!result) {
    return res.status(401).json({ error: 'Invalid credentials' });
  }

  res.json(result);
}