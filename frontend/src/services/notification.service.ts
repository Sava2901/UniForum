import api from './api';

export interface NotificationDto {
    id: number;
    message: string;
    type: string;
    relatedEntityId: number;
    read: boolean;
    timestamp: string;
}

/**
 * Service for handling user notifications.
 */
class NotificationService {
    /**
     * Retrieves all notifications for the current user.
     */
    getUserNotifications() {
        return api.get<NotificationDto[]>('/notifications');
    }

    /**
     * Marks a notification as read.
     * @param id The ID of the notification
     */
    markAsRead(id: number) {
        return api.post(`/notifications/${id}/read`);
    }
}

export default new NotificationService();
