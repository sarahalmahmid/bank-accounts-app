import { Router } from 'express';
import { requireAuth } from '../middleware/requireAuth';
import * as accountsController from '../controllers/accounts.controller';

const router = Router();

router.use(requireAuth);
router.get('/', accountsController.list);
router.get('/:accountNumber', accountsController.get);
router.patch('/:accountNumber', accountsController.updateNickname);

export default router;