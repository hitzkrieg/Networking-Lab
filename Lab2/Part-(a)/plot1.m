array1 = dlmread('data1.txt', ' ')
no_sources = array1(:, [1])
pc_packets_lost = array1(:, [2])
avg_delay = array1(:, [3])

plot(no_sources,pc_packets_lost)