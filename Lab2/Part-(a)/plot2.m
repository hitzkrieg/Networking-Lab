array1 = dlmread('data2.txt', ' ')
packet_gen_val = array1(:, [1])
avg_delay = array1(:, [2])
pc_packets_dropped = array1(:, [3])

plot(packet_gen_val, avg_delay)