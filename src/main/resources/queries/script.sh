
# You can add environment variables in server with the following method, please replace 'VARIABLE_NAME' and 'VARIABLE_VALUE' with your actual variable name and value respectively.

conf t
line vty 0 15
password cisco
login local
exit
username cisco privilege 15 secret cisco
ip domain-name pishgaman.local
crypto key generate rsa modulus 1024
ip ssh version 2
exit
write memory

enable
configure terminal
ip default-gateway 192.168.5.2
end
write memory

Current configuration : 3553 bytes
!
! Last configuration change at 16:27:41 IRST Sun Jun 9 2024
! NVRAM config last updated at 16:21:05 IRST Sun Jun 9 2024
!
version 12.2
no service pad
service timestamps debug datetime msec
service timestamps log datetime msec
no service password-encryption
!
hostname Switch
!
boot-start-marker
boot-end-marker
!
enable password <removed>
!
username cisco privilege 15 secret 5 <removed>
!
!
no aaa new-model
clock timezone IRST 3 30
switch 1 provision ws-c3750-24p
system mtu routing 1500
ip domain-name pishgaman.local
!
!
!
!
crypto pki trustpoint TP-self-signed-214965504
 enrollment selfsigned
 subject-name cn=IOS-Self-Signed-Certificate-214965504
 revocation-check none
 rsakeypair TP-self-signed-214965504
crypto pki certificate chain TP-self-signed-214965504
 certificate self-signed 01
  3082023D 308201A6 A0030201 02020101 300D0609 2A864886 F70D0101 04050030
  30312E30 2C060355 04031325 494F532D 53656C66 2D536967 6E65642D 43657274
  69666963 6174652D 32313439 36353530 34301E17 0D393330 33303130 30303134
  345A170D 32303031 30313030 30303030 5A303031 2E302C06 03550403 1325494F
  532D5365 6C662D53 69676E65 642D4365 72746966 69636174 652D3231 34393635
  35303430 819F300D 06092A86 4886F70D 01010105 0003818D 00308189 02818100
  EB1301BF 9050A4BB 72A117BC 85DD77E4 AB73FB3C B3F206ED C1C51FFD A3242C21
  46898BD4 4845B6A4 6D8FE57D FDE0786E 61C04ADA 024F6AA1 6EB54027 B27B9504
  BE84E7D2 3F87C45B 75E91D66 0312DF0E 9228D87D 7D05E099 5533E7B4 8574AE82
  90EF109F CC2A2680 2BAB2C61 59786386 1CD9FB22 762D202F F8D160ED 3CA0058F
  02030100 01A36730 65300F06 03551D13 0101FF04 05300301 01FF3012 0603551D
  11040B30 09820753 77697463 682E301F 0603551D 23041830 1680148C 1995D27D
  0945081F FB307CBB 64B867CD 22853230 1D060355 1D0E0416 04148C19 95D27D09
  45081FFB 307CBB64 B867CD22 8532300D 06092A86 4886F70D 01010405 00038181
  00C431D1 C942A824 50DFF825 E4C03573 F7B12B8D 0E0362A2 4DE74BB8 9D7F3DED
  3EE18C60 6DDC1F03 55920E3B 664D011E DB7B5393 9E853B68 616F96D2 2079B58A
  48585EC4 A100F1BE EA6186C8 23AAC018 F69478D6 A76B0173 F6C4B157 86B7BDAD
  41292F98 1AAAAB3B E1BAD2C8 0631493C 0488EE40 F43F7DEC 55C17FB5 72EA4D9A 43
  quit
!
!
!
spanning-tree mode pvst
spanning-tree extend system-id
!
vlan internal allocation policy ascending
!
ip ssh version 2
!
!
interface FastEthernet1/0/1
!
interface FastEthernet1/0/2
!
interface FastEthernet1/0/3
!
interface FastEthernet1/0/4
!
interface FastEthernet1/0/5
!
interface FastEthernet1/0/6
!
interface FastEthernet1/0/7
!
interface FastEthernet1/0/8
!
interface FastEthernet1/0/9
!
interface FastEthernet1/0/10
!
interface FastEthernet1/0/11
!
interface FastEthernet1/0/12
!
interface FastEthernet1/0/13
!
interface FastEthernet1/0/14
!
interface FastEthernet1/0/15
!
interface FastEthernet1/0/16
!
interface FastEthernet1/0/17
!
interface FastEthernet1/0/18
!
interface FastEthernet1/0/19
!
interface FastEthernet1/0/20
!
interface FastEthernet1/0/21
!
interface FastEthernet1/0/22
 switchport access vlan 100
!
interface FastEthernet1/0/23
 switchport access vlan 100
 switchport mode access
!
interface FastEthernet1/0/24
!
interface GigabitEthernet1/0/1
!
interface GigabitEthernet1/0/2
!
interface Vlan1
 ip address 192.168.1.28 255.255.255.0
!
interface Vlan5
 no ip address
!
interface Vlan100
 ip address 192.168.5.100 255.255.255.0
!
ip default-gateway 192.168.5.2
ip classless
ip http server
ip http secure-server
!
snmp-server community <removed> RO
!
!
line con 0
line vty 0 4
 password <removed>
 login local
line vty 5 15
 password <removed>
 login local
!
end


------------------ show stacks ------------------


Minimum process stacks:
 Free/Size   Name
 4904/6000   hulc_flash init
 5276/6000   MDFS LC IPC Init Process
 5720/6000   CDP BLOB
 8512/9000   IP Background
 8684/9000   EEM ED RF
 4192/6000   EEM Shell Director
 9848/12000  master cfg mgr init process
 5596/6000   vqpc_shim_create_addr_tbl
 5580/6000   SPAN Subsystem
 5584/6000   bulk config process
 5552/6000   hpm bulk_vlan_state process
 8460/12000  Init
58800/60000  EEM Auto Registration Proc
 5552/6000   SASL MAIN
 5296/6000   LICENSE AGENT DEFAULT
 5700/6000   MDFS Reload
 8700/9000   cdp init process
 5408/6000   RADIUS INITCONFIG
 5568/6000   Hulc Backup Interface Process
 5660/6000   URPF stats
 2532/3000   Rom Random Update Process
35068/36000  TCP Command
10212/12000  SSH Process
10508/12000  Virtual Exec
16940/24000  HTTP CP

Interrupt level stacks:
Level    Called Unused/Size  Name
  4  1480640435   8248/9000  NETWORK INTERFACE CHIP
  5          47   8856/9000  SUPERVISOR EXCEPTIONS
  6       43499   8936/9000  NS16550 VECTOR

------------------ show interfaces ------------------


Vlan1 is up, line protocol is up
  Hardware is EtherSVI, address is 0027.0cd0.1d40 (bia 0027.0cd0.1d40)
  Internet address is 192.168.1.28/24
  MTU 1500 bytes, BW 1000000 Kbit, DLY 10 usec,
     reliability 255/255, txload 1/255, rxload 1/255
  Encapsulation ARPA, loopback not set
  Keepalive not supported
  ARP type: ARPA, ARP Timeout 04:00:00
  Last input 00:00:00, output 00:41:25, output hang never
  Last clearing of "show interface" counters never
  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0
  Queueing strategy: fifo
  Output queue: 0/40 (size/max)
  5 minute input rate 12000 bits/sec, 16 packets/sec
  5 minute output rate 0 bits/sec, 0 packets/sec
     21198152 packets input, 1583030266 bytes, 0 no buffer
     Received 0 broadcasts (0 IP multicasts)
     0 runts, 0 giants, 0 throttles
     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored
     1956 packets output, 1334791 bytes, 0 underruns
     0 output errors, 2 interface resets
     0 output buffer failures, 0 output buffers swapped out
Vlan5 is up, line protocol is down
  Hardware is EtherSVI, address is 0027.0cd0.1d41 (bia 0027.0cd0.1d41)
  MTU 1500 bytes, BW 1000000 Kbit, DLY 10 usec,
     reliability 255/255, txload 1/255, rxload 1/255
  Encapsulation ARPA, loopback not set
  Keepalive not supported
  ARP type: ARPA, ARP Timeout 04:00:00
  Last input never, output never, output hang never
  Last clearing of "show interface" counters never
  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0
  Queueing strategy: fifo
  Output queue: 0/40 (size/max)
  5 minute input rate 0 bits/sec, 0 packets/sec
  5 minute output rate 0 bits/sec, 0 packets/sec
     0 packets input, 0 bytes, 0 no buffer
     Received 0 broadcasts (0 IP multicasts)
     0 runts, 0 giants, 0 throttles
     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored
     0 packets output, 0 bytes, 0 underruns
     0 output errors, 2 interface resets
     0 output buffer failures, 0 output buffers swapped out
Vlan100 is up, line protocol is up
  Hardware is EtherSVI, address is 0027.0cd0.1d42 (bia 0027.0cd0.1d42)
  Internet address is 192.168.5.100/24
  MTU 1500 bytes, BW 1000000 Kbit, DLY 10 usec,
     reliability 255/255, txload 1/255, rxload 1/255
  Encapsulation ARPA, loopback not set
  Keepalive not supported
  ARP type: ARPA, ARP Timeout 04:00:00
  Last input 00:00:00, output 00:00:00, output hang never
  Last clearing of "show interface" counters never
  Input queue: 1/75/0/0 (size/max/drops/flushes); Total output drops: 0
  Queueing strategy: fifo
  Output queue: 0/40 (size/max)
  5 minute input rate 2000 bits/sec, 2 packets/sec
  5 minute output rate 2000 bits/sec, 2 packets/sec
     583395 packets input, 63904883 bytes, 0 no buffer
     Received 0 broadcasts (0 IP multicasts)
     0 runts, 0 giants, 0 throttles
     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored
     198951 packets output, 18728127 bytes, 0 underruns
     0 output errors, 2 interface resets
     0 output buffer failures, 0 output buffers swapped out
FastEthernet1/0/1 is down, line protocol is down (notconnect)
  Hardware is Fast Ethernet, address is 0027.0cd0.1d03 (bia 0027.0cd0.1d03)
  MTU 1500 bytes, BW 10000 Kbit, DLY 1000 usec,
     reliability 255/255, txload 1/255, rxload 1/255
  Encapsulation ARPA, loopback not set
  Keepalive set (10 sec)
  Auto-duplex, Auto-speed, media type is 10/100BaseTX
  input flow-control is off, output flow-control is unsupported
  ARP type: ARPA, ARP Timeout 04:00:00
  Last input never, output never, output hang never
  Last clearing of "show interface" counters never
  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0
  Queueing strategy: fifo
  Output queue: 0/40 (size/max)
  5 minute input rate 0 bits/sec, 0 packets/sec
  5 minute output rate 0 bits/sec, 0 packets/sec
     0 packets input, 0 bytes, 0 no buffer
     Received 0 broadcasts (0 multicasts)
     0 runts, 0 giants, 0 throttles
     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored
     0 watchdog, 0 multicast, 0 pause input
     0 input packets with dribble condition detected
     0 packets output, 0 bytes, 0 underruns
     0 output errors, 0 collisions, 1 interface resets
     0 babbles, 0 late collision, 0 deferred
     0 lost carrier, 0 no carrier, 0 PAUSE output
     0 output buffer failures, 0 output buffers swapped out
FastEthernet1/0/2 is down, line protocol is down (notconnect)
  Hardware is Fast Ethernet, address is 0027.0cd0.1d04 (bia 0027.0cd0.1d04)
  MTU 1500 bytes, BW 10000 Kbit, DLY 1000 usec,
     reliability 255/255, txload 1/255, rxload 1/255
  Encapsulation ARPA, loopback not set
  Keepalive set (10 sec)
  Auto-duplex, Auto-speed, media type is 10/100BaseTX
  input flow-control is off, output flow-control is unsupported
  ARP type: ARPA, ARP Timeout 04:00:00
  Last input never, output never, output hang never
  Last clearing of "show interface" counters never
  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0
  Queueing strategy: fifo
  Output queue: 0/40 (size/max)
  5 minute input rate 0 bits/sec, 0 packets/sec
  5 minute output rate 0 bits/sec, 0 packets/sec
     0 packets input, 0 bytes, 0 no buffer
     Received 0 broadcasts (0 multicasts)
     0 runts, 0 giants, 0 throttles
     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored
     0 watchdog, 0 multicast, 0 pause input
     0 input packets with dribble condition detected
     0 packets output, 0 bytes, 0 underruns
     0 output errors, 0 collisions, 1 interface resets
     0 babbles, 0 late collision, 0 deferred
     0 lost carrier, 0 no carrier, 0 PAUSE output
     0 output buffer failures, 0 output buffers swapped out
FastEthernet1/0/3 is up, line protocol is up (connected)
  Hardware is Fast Ethernet, address is 0027.0cd0.1d05 (bia 0027.0cd0.1d05)
  MTU 1500 bytes, BW 100000 Kbit, DLY 100 usec,
     reliability 255/255, txload 1/255, rxload 1/255
  Encapsulation ARPA, loopback not set
  Keepalive set (10 sec)
  Full-duplex, 100Mb/s, media type is 10/100BaseTX
  input flow-control is off, output flow-control is unsupported
  ARP type: ARPA, ARP Timeout 04:00:00
  Last input 00:00:07, output 00:00:00, output hang never
  Last clearing of "show interface" counters never
  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 3178
  Queueing strategy: fifo
  Output queue: 0/40 (size/max)
  5 minute input rate 31000 bits/sec, 23 packets/sec
  5 minute output rate 390000 bits/sec, 32 packets/sec
     559059243 packets input, 95089621641 bytes, 0 no buffer
     Received 4139838 broadcasts (1208070 multicasts)
     0 runts, 0 giants, 0 throttles
     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored
     0 watchdog, 1208070 multicast, 0 pause input
     0 input packets with dribble condition detected
     714120602 packets output, 870187443591 bytes, 0 underruns
     0 output errors, 0 collisions, 1 interface resets
     0 babbles, 0 late collision, 0 deferred
     0 lost carrier, 0 no carrier, 0 PAUSE output
     0 output buffer failures, 0 output buffers swapped out
FastEthernet1/0/4 is down, line protocol is down (notconnect)
  Hardware is Fast Ethernet, address is 0027.0cd0.1d06 (bia 0027.0cd0.1d06)
  MTU 1500 bytes, BW 10000 Kbit, DLY 1000 usec,
     reliability 255/255, txload 1/255, rxload 1/255
  Encapsulation ARPA, loopback not set
  Keepalive set (10 sec)
  Auto-duplex, Auto-speed, media type is 10/100BaseTX
  input flow-control is off, output flow-control is unsupported
  ARP type: ARPA, ARP Timeout 04:00:00
  Last input never, output never, output hang never
  Last clearing of "show interface" counters never
  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0
  Queueing strategy: fifo
  Output queue: 0/40 (size/max)
  5 minute input rate 0 bits/sec, 0 packets/sec
  5 minute output rate 0 bits/sec, 0 packets/sec
     0 packets input, 0 bytes, 0 no buffer
     Received 0 broadcasts (0 multicasts)
     0 runts, 0 giants, 0 throttles
     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored
     0 watchdog, 0 multicast, 0 pause input
     0 input packets with dribble condition detected
     0 packets output, 0 bytes, 0 underruns
     0 output errors, 0 collisions, 1 interface resets
     0 babbles, 0 late collision, 0 deferred
     0 lost carrier, 0 no carrier, 0 PAUSE output
     0 output buffer failures, 0 output buffers swapped out
FastEthernet1/0/5 is down, line protocol is down (notconnect)
  Hardware is Fast Ethernet, address is 0027.0cd0.1d07 (bia 0027.0cd0.1d07)
  MTU 1500 bytes, BW 10000 Kbit, DLY 1000 usec,
     reliability 255/255, txload 1/255, rxload 1/255
  Encapsulation ARPA, loopback not set
  Keepalive set (10 sec)
  Auto-duplex, Auto-speed, media type is 10/100BaseTX
  input flow-control is off, output flow-control is unsupported
  ARP type: ARPA, ARP Timeout 04:00:00
  Last input 22w3d, output 22w1d, output hang never
  Last clearing of "show interface" counters never
  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0
  Queueing strategy: fifo
  Output queue: 0/40 (size/max)
  5 minute input rate 0 bits/sec, 0 packets/sec
  5 minute output rate 0 bits/sec, 0 packets/sec
     2021 packets input, 632075 bytes, 0 no buffer
     Received 581 broadcasts (335 multicasts)
     0 runts, 0 giants, 0 throttles
     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored
     0 watchdog, 335 multicast, 0 pause input
     0 input packets with dribble condition detected
     240545 packets output, 20860332 bytes, 0 underruns
     0 output errors, 0 collisions, 1 interface resets
     0 babbles, 0 late collision, 0 deferred
     0 lost carrier, 0 no carrier, 0 PAUSE output
     0 output buffer failures, 0 output buffers swapped out
FastEthernet1/0/6 is down, line protocol is down (notconnect)
  Hardware is Fast Ethernet, address is 0027.0cd0.1d08 (bia 0027.0cd0.1d08)
  MTU 1500 bytes, BW 10000 Kbit, DLY 1000 usec,
     reliability 255/255, txload 1/255, rxload 1/255
  Encapsulation ARPA, loopback not set
  Keepalive set (10 sec)
  Auto-duplex, Auto-speed, media type is 10/100BaseTX
  input flow-control is off, output flow-control is unsupported
  ARP type: ARPA, ARP Timeout 04:00:00
  Last input never, output never, output hang never
  Last clearing of "show interface" counters never
  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0
  Queueing strategy: fifo
  Output queue: 0/40 (size/max)
  5 minute input rate 0 bits/sec, 0 packets/sec
  5 minute output rate 0 bits/sec, 0 packets/sec
     0 packets input, 0 bytes, 0 no buffer
     Received 0 broadcasts (0 multicasts)
     0 runts, 0 giants, 0 throttles
     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored
     0 watchdog, 0 multicast, 0 pause input
     0 input packets with dribble condition detected
     0 packets output, 0 bytes, 0 underruns
     0 output errors, 0 collisions, 1 interface resets
     0 babbles, 0 late collision, 0 deferred
     0 lost carrier, 0 no carrier, 0 PAUSE output
     0 output buffer failures, 0 output buffers swapped out
FastEthernet1/0/7 is down, line protocol is down (notconnect)
  Hardware is Fast Ethernet, address is 0027.0cd0.1d09 (bia 0027.0cd0.1d09)
  MTU 1500 bytes, BW 10000 Kbit, DLY 1000 usec,
     reliability 255/255, txload 1/255, rxload 1/255
  Encapsulation ARPA, loopback not set
  Keepalive set (10 sec)
  Auto-duplex, Auto-speed, media type is 10/100BaseTX
  input flow-control is off, output flow-control is unsupported
  ARP type: ARPA, ARP Timeout 04:00:00
  Last input never, output never, output hang never
  Last clearing of "show interface" counters never
  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0
  Queueing strategy: fifo
  Output queue: 0/40 (size/max)
  5 minute input rate 0 bits/sec, 0 packets/sec
  5 minute output rate 0 bits/sec, 0 packets/sec
     0 packets input, 0 bytes, 0 no buffer
     Received 0 broadcasts (0 multicasts)
     0 runts, 0 giants, 0 throttles
     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored
     0 watchdog, 0 multicast, 0 pause input
     0 input packets with dribble condition detected
     0 packets output, 0 bytes, 0 underruns
     0 output errors, 0 collisions, 1 interface resets
     0 babbles, 0 late collision, 0 deferred
     0 lost carrier, 0 no carrier, 0 PAUSE output
     0 output buffer failures, 0 output buffers swapped out
FastEthernet1/0/8 is down, line protocol is down (notconnect)
  Hardware is Fast Ethernet, address is 0027.0cd0.1d0a (bia 0027.0cd0.1d0a)
  MTU 1500 bytes, BW 10000 Kbit, DLY 1000 usec,
     reliability 255/255, txload 1/255, rxload 1/255
  Encapsulation ARPA, loopback not set
  Keepalive set (10 sec)
  Auto-duplex, Auto-speed, media type is 10/100BaseTX
  input flow-control is off, output flow-control is unsupported
  ARP type: ARPA, ARP Timeout 04:00:00
  Last input never, output never, output hang never
  Last clearing of "show interface" counters never
  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0
  Queueing strategy: fifo
  Output queue: 0/40 (size/max)
  5 minute input rate 0 bits/sec, 0 packets/sec
  5 minute output rate 0 bits/sec, 0 packets/sec
     0 packets input, 0 bytes, 0 no buffer
     Received 0 broadcasts (0 multicasts)
     0 runts, 0 giants, 0 throttles
     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored
     0 watchdog, 0 multicast, 0 pause input
     0 input packets with dribble condition detected
     0 packets output, 0 bytes, 0 underruns
     0 output errors, 0 collisions, 1 interface resets
     0 babbles, 0 late collision, 0 deferred
     0 lost carrier, 0 no carrier, 0 PAUSE output
     0 output buffer failures, 0 output buffers swapped out
FastEthernet1/0/9 is down, line protocol is down (notconnect)
  Hardware is Fast Ethernet, address is 0027.0cd0.1d0b (bia 0027.0cd0.1d0b)
  MTU 1500 bytes, BW 10000 Kbit, DLY 1000 usec,
     reliability 255/255, txload 1/255, rxload 1/255
  Encapsulation ARPA, loopback not set
  Keepalive set (10 sec)
  Auto-duplex, Auto-speed, media type is 10/100BaseTX
  input flow-control is off, output flow-control is unsupported
  ARP type: ARPA, ARP Timeout 04:00:00
  Last input never, output never, output hang never
  Last clearing of "show interface" counters never
  Input queue: 0/75/0/0 (size/max/drops/flushes); Total output drops: 0
  Queueing strategy: fifo
  Output queue: 0/40 (size/max)
  5 minute input rate 0 bits/sec, 0 packets/sec
  5 minute output rate 0 bits/sec, 0 packets/sec
     0 packets input, 0 bytes, 0 no buffer
     Received 0 broadcasts (0 multicasts)
     0 runts, 0 giants, 0 throttles
     0 input errors, 0 CRC, 0 frame, 0 overrun, 0 ignored
     0 watchdog, 0 multicast, 0 pause input
     0 input packets with dribble condition detected
     0 packets output, 0 bytes, 0 underruns
     0 output errors, 0 collisions, 1 interface resets
     0 babbles, 0 late collision, 0 deferred
     0 lost carrier, 0 no carrier, 0 PAUSE output
     0 output buffer failures, 0 output buffers swapped out
FastEthernet1/0/10 is down, line protocol is down (notconnect)
FastEthernet1/0/11 is down, line protocol is down (notconnect)
FastEthernet1/0/12 is down, line protocol is down (notconnect)
FastEthernet1/0/13 is down, line protocol is down (notconnect)
FastEthernet1/0/14 is down, line protocol is down (notconnect)
FastEthernet1/0/15 is up, line protocol is up (connected)
FastEthernet1/0/16 is down, line protocol is down (notconnect)
FastEthernet1/0/17 is down, line protocol is down (notconnect)
FastEthernet1/0/18 is down, line protocol is down (notconnect)
FastEthernet1/0/19 is up, line protocol is up (connected)
FastEthernet1/0/20 is down, line protocol is down (notconnect)
FastEthernet1/0/21 is down, line protocol is down (notconnect)
FastEthernet1/0/22 is down, line protocol is down (notconnect)
FastEthernet1/0/23 is up, line protocol is up (connected)
FastEthernet1/0/24 is down, line protocol is down (notconnect)
GigabitEthernet1/0/1 is down, line protocol is down (notconnect)
GigabitEthernet1/0/2 is down, line protocol is down (notconnect)