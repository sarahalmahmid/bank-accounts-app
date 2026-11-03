import { Router } from 'express';
import { requireAuth } from '../middleware/requireAuth';
import * as accountsController from '../controllers/accounts.controller';

const router = Router();

router.use(requireAuth);
router.get('/', accountsController.list);

export default router;