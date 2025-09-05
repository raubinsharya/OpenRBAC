#!/bin/bash

# Docker Nuclear Cleanup Script
# This script will remove ALL Docker containers, images, volumes, and networks

echo "🧹 Starting Docker nuclear cleanup..."

# Stop all running containers
echo "⏹️  Stopping all running containers..."
docker stop $(docker ps -q) 2>/dev/null || echo "No running containers to stop"

# Stop docker-compose services if compose file exists
if [ -f "docker-compose.yml" ] || [ -f "docker-compose.yaml" ]; then
    echo "📦 Stopping docker-compose services..."
    docker-compose down 2>/dev/null || echo "No compose services to stop"
fi

# Remove all containers (running and stopped)
echo "🗑️  Removing all containers..."
docker rm -f $(docker ps -aq) 2>/dev/null || echo "No containers to remove"

# Remove all images
echo "🖼️  Removing all images..."
docker rmi -f $(docker images -aq) 2>/dev/null || echo "No images to remove"

# Remove all volumes
echo "💾 Removing all volumes..."
docker volume rm $(docker volume ls -q) 2>/dev/null || echo "No volumes to remove"

# Remove all custom networks (keep default ones)
echo "🌐 Removing unused networks..."
docker network prune -f 2>/dev/null || echo "No networks to remove"

# Final system cleanup
echo "🧽 Running final system cleanup..."
docker system prune -af 2>/dev/null || echo "System already clean"

# Show final status
echo ""
echo "✅ Docker cleanup completed!"
echo ""
echo "📊 Current Docker status:"
echo "Containers: $(docker ps -a --format 'table {{.Names}}' | wc -l | xargs echo) - 1" 2>/dev/null || echo "Containers: 0"
echo "Images: $(docker images --format 'table {{.Repository}}' | wc -l | xargs echo) - 1" 2>/dev/null || echo "Images: 0"
echo "Volumes: $(docker volume ls --format 'table {{.Name}}' | wc -l | xargs echo) - 1" 2>/dev/null || echo "Volumes: 0"
echo ""
echo "🚀 Ready for fresh Docker setup!"