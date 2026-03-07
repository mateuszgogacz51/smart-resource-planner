export interface Application {
  id: number;
  title: string;
  description: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'ASSIGNED'; // Statusy zgodne z Twoim planem
  createdAt: string; // Data utworzenia
  assignedTo?: string; // Opcjonalne: login pracownika, który zajmuje się wnioskiem
  ownerUsername: string; // Login użytkownika, który złożył wniosek
  comments?: string[]; // Lista komentarzy do wniosku
}