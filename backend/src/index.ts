import express, { Request, Response, NextFunction } from 'express';
import { config } from './config';
import authRoutes from './routes/auth.routes';
import accountsRoutes from './routes/accounts.routes';

const app = express();
app.use(express.json());

app.get('/health', (req, res) => {
  res.json({ status: 'ok' });
});

app.use('/api/auth', authRoutes);

app.use('/api/accounts', accountsRoutes);

app.use((req: Request, res: Response) => {
  res.status(404).json({ error: 'Not found' });
});

app.use((err: unknown, req: Request, res: Response, next: NextFunction) => {
  console.error(err);
  res.status(500).json({ error: 'Internal server error' });
});

app.listen(config.port, () => {
  console.log(`API running on http://localhost:${config.port}`);
});