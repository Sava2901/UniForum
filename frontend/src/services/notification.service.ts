import api from './api';

export interface NotificationDto {
    id: number;
    message: string;
    type: string;
    relatedEntityId: number;
    read: boolean;
    timestamp: string;
}

class NotificationService {
    getUserNotifications() {
        return api.get<NotificationDto[]>('/notifications');
    }

    markAsRead(id: number) {
        return api.post(`/notifications/${id}/read`);
    }
}

export default new NotificationService();
