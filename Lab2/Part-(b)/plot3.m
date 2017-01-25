array1 = dlmread('data3.txt', ' ')
queue_size = array1(:, [1])
pc_packets_dropped = array1(:, [2])
avg_delay = array1(:, [3])

plot(queue_size, pc_packets_dropped)