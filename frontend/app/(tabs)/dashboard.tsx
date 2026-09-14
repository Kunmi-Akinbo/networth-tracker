import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, ScrollView, RefreshControl } from 'react-native';
import axios from 'axios';
import { useAuth } from '../../context/AuthContext';

const API_BASE_URL = 'http://localhost:8080/api';

interface AnalyticsData {
  currentNetWorth: number;
  totalAssets: number;
  totalLiabilities: number;
  netWorthChange: number;
  netWorthChangePercentage: number;
}

export default function DashboardScreen() {
  const [analytics, setAnalytics] = useState<AnalyticsData | null>(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const { user } = useAuth();

  const fetchAnalytics = async () => {
    if (!user) return;
    
    try {
      const response = await axios.get(`${API_BASE_URL}/analytics?userId=${user.id}&months=12`);
      setAnalytics(response.data);
    } catch (error) {
      console.error('Failed to fetch analytics:', error);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    fetchAnalytics();
  }, []);

  const onRefresh = () => {
    setRefreshing(true);
    fetchAnalytics();
  };

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(value);
  };

  if (loading) {
    return (
      <View style={styles.container}>
        <Text style={styles.loadingText}>Loading...</Text>
      </View>
    );
  }

  return (
    <ScrollView
      style={styles.container}
      refreshControl={
        <RefreshControl refreshing={refreshing} onRefresh={onRefresh} />
      }
    >
      <View style={styles.card}>
        <Text style={styles.cardTitle}>Net Worth</Text>
        <Text style={styles.netWorthValue}>
          {analytics ? formatCurrency(analytics.currentNetWorth) : '$0.00'}
        </Text>
        {analytics && analytics.netWorthChangePercentage !== 0 && (
          <Text style={[
            styles.changeText,
            analytics.netWorthChangePercentage >= 0 ? styles.positive : styles.negative
          ]}>
            {analytics.netWorthChangePercentage >= 0 ? '+' : ''}
            {analytics.netWorthChangePercentage.toFixed(2)}%
          </Text>
        )}
      </View>

      <View style={styles.row}>
        <View style={[styles.card, styles.halfCard]}>
          <Text style={styles.cardTitle}>Assets</Text>
          <Text style={styles.cardValue}>
            {analytics ? formatCurrency(analytics.totalAssets) : '$0.00'}
          </Text>
        </View>
        <View style={[styles.card, styles.halfCard]}>
          <Text style={styles.cardTitle}>Liabilities</Text>
          <Text style={styles.cardValue}>
            {analytics ? formatCurrency(analytics.totalLiabilities) : '$0.00'}
          </Text>
        </View>
      </View>

      <View style={styles.card}>
        <Text style={styles.cardTitle}>Monthly Change</Text>
        <Text style={styles.cardValue}>
          {analytics ? formatCurrency(analytics?.netWorthChange || 0) : '$0.00'}
        </Text>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
    padding: 15,
  },
  loadingText: {
    fontSize: 16,
    textAlign: 'center',
    marginTop: 20,
    color: '#666',
  },
  card: {
    backgroundColor: '#fff',
    borderRadius: 12,
    padding: 20,
    marginBottom: 15,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  halfCard: {
    flex: 1,
  },
  row: {
    flexDirection: 'row',
    gap: 15,
  },
  cardTitle: {
    fontSize: 14,
    color: '#666',
    marginBottom: 8,
  },
  cardValue: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#333',
  },
  netWorthValue: {
    fontSize: 36,
    fontWeight: 'bold',
    color: '#007AFF',
  },
  changeText: {
    fontSize: 16,
    marginTop: 8,
    fontWeight: '600',
  },
  positive: {
    color: '#34C759',
  },
  negative: {
    color: '#FF3B30',
  },
});
